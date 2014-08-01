package com.github.scrud.android.generate

import collection.immutable.List
import xml._
import com.github.scrud.android.view.AndroidConversions
import com.github.scrud.{EntityNavigation, EntityName, EntityType}
import com.github.scrud.util.{Logging, Common}
import com.github.scrud.android.{CrudAndroidApplicationLike, CrudActivity, AndroidPlatformDriver, CrudAndroidApplication}
import com.github.scrud.android.backup.CrudBackupAgent
import scala.reflect.io.{File, Directory}
import com.github.scrud.platform.representation.{EditUI, SelectUI, SummaryUI, DetailUI}
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.platform._

/** A UI Generator for a CrudTypes.
  * @author Eric Pabst (epabst@gmail.com)
  */

class CrudUIGenerator(val workingDir: Directory, overwrite: Boolean) extends Logging {
  def this(overwrite: Boolean) {
    this(Directory.Current.get, overwrite)
  }

  protected def logTag = Common.logTag
  private val lineSeparator = System.getProperty("line.separator")
  private val androidScope: NamespaceBinding = <TextView xmlns:android="http://schemas.android.com/apk/res/android"/>.scope
  private[generate] val prettyPrinter = new PrettyPrinter(80, 2) {
    override protected def traverse(node: Node, scope: NamespaceBinding, indent: Int) {
      node match {
        // Eliminate extra whitespace between elements
        case Text(text) if text.trim().isEmpty => super.traverse(Text(""), scope, indent)
        // Collapse end-tags if there are no children unless it is the top-level element
        case elem: Elem if elem.child.isEmpty && indent > 0 => makeBox(indent, leafTag(elem))
        case _ => super.traverse(node, scope, indent)
      }
    }
  }

  private def writeXmlToFile(file: File, xml: Elem) {
    file.parent.createDirectory()
    if (overwrite || !file.exists) {
      file.writeAll( """<?xml version="1.0" encoding="utf-8"?>""", lineSeparator, prettyPrinter.format(xml))
      println("Wrote " + file)
    } else {
      println("Skipped writing to " + file + " since it already exists and overwrite=false")
    }
  }

  def generateAndroidManifest(entityTypeMap: EntityTypeMap, androidApplicationClass: Class[_ <: CrudAndroidApplicationLike],
                              backupAgentClass: Class[_ <: CrudBackupAgent]): Elem = {
    if (!classOf[CrudAndroidApplication].isAssignableFrom(androidApplicationClass)) {
      throw new IllegalArgumentException(androidApplicationClass + " does not extend CrudAndroidApplication")
    }
    generateAndroidManifest(entityTypeMap, androidApplicationClass.getPackage.getName, androidApplicationClass.getSimpleName, backupAgentClass.getName)
  }

  def generateAndroidManifest(entityTypeMap: EntityTypeMap, androidApplicationPackageName: String, androidApplicationClassSimpleName: String,
                              backupAgentClassName: String): Elem = {
    val applicationPackageName = androidApplicationPackageName
    val activityNames = Seq(findAndroidPlatformDriverFor(entityTypeMap).map(_.activityClass).getOrElse(classOf[CrudActivity]).getName)
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
              package={applicationPackageName}>
      <application android:label="@string/app_name" android:icon="@drawable/ic_launcher"
                   android:name={androidApplicationPackageName + "." + androidApplicationClassSimpleName}
                   android:theme="@android:style/Theme.NoTitleBar"
                   android:backupAgent={backupAgentClassName} android:restoreAnyVersion="true">
        <meta-data android:name="com.google.android.backup.api_key"
                   android:value="TODO: get a backup key from http://code.google.com/android/backup/signup.html and put it here."/>
        <activity android:name={activityNames.head} android:label="@string/app_name">
          <intent-filter>
            <action android:name="android.intent.action.MAIN"/>
            <category android:name="android.intent.category.LAUNCHER"/>
          </intent-filter>
        </activity>
        {activityNames.tail.map { name => <activity android:name={name} android:label="@string/app_name"/>}}
        <provider android:authorities={AndroidConversions.authorityFor(androidApplicationPackageName)}
                  android:name="com.github.scrud.android.persistence.LocalCrudContentProvider"
                  android:exported="false"
                  android:grantUriPermissions="false"
                  android:multiprocess="true"
                  android:syncable="false"/>
      </application>
      <uses-sdk android:minSdkVersion="16"/>
    </manifest>
  }

  private def findAndroidPlatformDriverFor(entityTypeMap: EntityTypeMap): Option[AndroidPlatformDriver] = {
    entityTypeMap.platformDriver match {
     case androidPlatformDriver: AndroidPlatformDriver => Some(androidPlatformDriver)
     case _ => None
    }
  }

  def generateValueStrings(entityInfo: EntityTypeViewInfo): NodeSeq = {
    import entityInfo._
    val addSeq = if (entityTypeMap.isCreatable(entityType)) <string name={"add_" + layoutPrefix}>Add {entityName.toDisplayableString}</string> else NodeSeq.Empty
    val editSeq = if (entityTypeMap.isSavable(entityType)) <string name={"edit_" + layoutPrefix}>Edit {entityName.toDisplayableString}</string> else NodeSeq.Empty
    val listSeq = if (entityTypeMap.isListable(entityType)) <string name={layoutPrefix + "_list"}>{entityName.toDisplayableString} List</string> else NodeSeq.Empty
    val deleteSeq = if (entityTypeMap.isDeletable(entityType)) <string name={"delete_" + layoutPrefix}>Delete {entityName.toDisplayableString}</string> else NodeSeq.Empty
    listSeq ++ addSeq ++ editSeq ++ deleteSeq
  }

  def attemptToEvaluate[T](f: => T): Option[T] =
    try {
      Some(f)
    } catch {
      case e: Throwable => debug(e.toString); None
    }

  def generateValueStrings(entityTypeMap: EntityTypeMap): Elem = {
    <resources>
      <string name="app_name">{entityTypeMap.applicationName.name}</string>
      {entityTypeMap.allEntityTypes.flatMap(entityType => generateValueStrings(EntityTypeViewInfo(entityType, entityTypeMap)))}
      <string name ="delete_item">Delete</string>
      <string name ="undo_delete">Undo Delete</string>
      <string name ="data_saved_notification">Saved.</string>
      <string name ="data_not_saved_since_invalid_notification">Not saved since invalid.</string>
    </resources>
  }

  def generateLayouts(entityTypeMap: EntityTypeMap,
                      androidApplicationClass: Class[_ <: CrudAndroidApplicationLike],
                      backupAgentClass: Class[_ <: CrudBackupAgent]) {
    generateLayouts(entityTypeMap, androidApplicationClass.getPackage.getName, androidApplicationClass.getSimpleName, backupAgentClass.getName)
  }

  def generateLayouts(entityTypeMap: EntityTypeMap,
                      androidApplicationPackageName: String, androidApplicationClassSimpleName: String,
                      backupAgentClassName: String) {
    val entityTypeInfos = entityTypeMap.allEntityTypes.map(EntityTypeViewInfo(_, entityTypeMap))
    val pickedEntityTypes: Seq[EntityType] = for {
      entityType <- entityTypeMap.allEntityTypes
      fieldDeclaration <- entityType.fieldDeclarations
      referencedEntityName <- fieldDeclaration.qualifiedType match {
        case entityName: EntityName => Some(entityName)
        case _ => None
      }
      referencedEntityType = entityTypeMap.entityType(referencedEntityName)
    } yield referencedEntityType
    entityTypeInfos.foreach(entityInfo => {
      val childViewInfos = entityTypeMap.downstreamEntityTypes(entityInfo.entityType).map(EntityTypeViewInfo(_, entityTypeMap))
      generateLayouts(entityInfo, childViewInfos, pickedEntityTypes)
    })
    writeXmlToFile((workingDir / "AndroidManifest.xml").toFile, generateAndroidManifest(entityTypeMap,
      androidApplicationPackageName, androidApplicationClassSimpleName, backupAgentClassName))
    writeXmlToFile((workingDir / "res" / "values" / "strings.xml").toFile, generateValueStrings(entityTypeMap))
  }

  protected[generate] def fieldLayoutForHeader(field: ViewIdFieldInfo, position: Int): Elem = {
    val textAppearance = if (position < 2) "?android:attr/textAppearanceLarge" else "?android:attr/textAppearanceSmall"
    val gravity = if (position % 2 == 0) "left" else "right"
    val layoutWidth = if (position % 2 == 0) "wrap_content" else "fill_parent"
    <TextView android:text={field.displayName} android:gravity={gravity}
              android:layout_width={layoutWidth}
              android:layout_height="wrap_content"
              android:paddingRight="3sp"
              android:textAppearance={textAppearance} style="@android:style/TextAppearance.Widget.TextView"/>
  }

  protected[generate] def fieldLayoutForRow(field: ViewIdFieldInfo, position: Int): NodeSeq =
    field.layoutForDisplayUI(position)

  protected def emptyListRenderedDifferently: Boolean = false

  protected def listLayout(entityInfo: EntityTypeViewInfo, childEntityInfos: Seq[EntityTypeViewInfo]) = {
    val entityTypeMap = entityInfo.entityTypeMap
    val addableEntityTypeInfos = if (entityTypeMap.isCreatable(entityInfo.entityType)) List(entityInfo) else childEntityInfos.filter(childInfo => entityTypeMap.isCreatable(childInfo.entityType))
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                  android:orientation="vertical"
                  android:layout_width="fill_parent"
                  android:layout_height="fill_parent">
      <ListView android:id={"@+id/" + entityInfo.entityName.toSnakeCase + "_list"}
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_weight="1.0"/>
      {if (emptyListRenderedDifferently || addableEntityTypeInfos.isEmpty)
        <TextView android:id={"@+id/" + entityInfo.entityName.toSnakeCase + "_emptyList"}
                  android:layout_width="wrap_content"
                  android:layout_height="wrap_content" android:layout_weight="1"
                  android:text={"Empty " + entityInfo.entityName.toDisplayableString + " List"}
                  android:textAppearance="?android:attr/textAppearanceLarge" style="@android:style/TextAppearance.Widget.TextView"/>
      }
      { addableEntityTypeInfos.map(addableEntityTypeInfo =>
        <Button android:id={"@+id/add_" + addableEntityTypeInfo.layoutPrefix + "_command"}
                android:text={"@string/add_" + addableEntityTypeInfo.layoutPrefix}
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:drawableLeft="@android:drawable/ic_input_add"/>
      )}
    </LinearLayout>
  }

  protected def headerLayout(entityInfo: EntityTypeViewInfo) =
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                  android:paddingTop="2dip"
                  android:paddingBottom="2dip"
                  android:paddingLeft="6dip"
                  android:paddingRight="6dip"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:orientation="vertical">
      <TextView android:layout_height="wrap_content" android:paddingRight="3sp" android:text={entityInfo.entityName.toDisplayableString + " List"}
                android:textAppearance="?android:attr/textAppearanceLarge" style="@android:style/TextAppearance.Widget.TextView"
                android:gravity="center" android:layout_width="match_parent"/>
    </LinearLayout>

  protected def rowLayout(fields: List[ViewIdFieldInfo]) =
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                  android:paddingTop="2dip"
                  android:paddingBottom="2dip"
                  android:paddingLeft="6dip"
                  android:paddingRight="6dip"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:minHeight="?android:attr/listPreferredItemHeight"
                  android:orientation="vertical">{
      fields.grouped(2).map { rowFields =>
        <LinearLayout android:layout_width="match_parent"
                      android:layout_height="wrap_content"
                      android:orientation="horizontal">
          {rowFields.map(field => fieldLayoutForRow(field, fields.indexOf(field)))}
        </LinearLayout>
      }
    }
    </LinearLayout>

  protected def pickLayout(fields: Seq[ViewIdFieldInfo]): NodeSeq = {
    fields.headOption.fold(NodeSeq.Empty) { field =>
      val layout = fieldLayoutForRow(field, 0)
      val adjusted = adjustHeadNode(layout, {
        case elem: Elem => elem.copy(scope = androidScope)
        case node => node
      })
      adjusted
    }
  }

  private def applyAttributes(xml: Node, attributes: MetaData): Node = xml match {
    case e: Elem => e % attributes
    case x => x
  }

  private def adjustHeadNode(xml: NodeSeq, f: Node => Node): NodeSeq = xml.headOption.map(f(_) +: xml.tail).getOrElse(xml)

  def entryLayout(fields: List[ViewIdFieldInfo]): Elem = {
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                 android:orientation="vertical"
                 android:layout_width="fill_parent"
                 android:layout_height="wrap_content">
      {fields.flatMap(field => field.layoutForEditUI(fields.indexOf(field)))}
    </LinearLayout>
  }

  private def writeLayoutFile(name: String, xml: Elem) {
    writeXmlToFile((workingDir / "res" / "layout" / (name + ".xml")).toFile, xml)
  }

  private def writeLayoutFileIfNotEmpty(name: String, xml: NodeSeq) {
    xml match {
      case NodeSeq.Empty => // Don't write the file
      case Seq(headNode) => headNode match {
        case headElem: Elem => writeLayoutFile(name, headElem)
      }
    }
  }

  def generateLayouts(entityTypeInfo: EntityTypeViewInfo, childTypeInfos: Seq[EntityTypeViewInfo], pickedEntityTypes: Seq[EntityType]) {
    println("Generating layout for " + entityTypeInfo.entityType)
    val entityTypeMap = entityTypeInfo.entityTypeMap
    lazy val entityInfo = EntityTypeViewInfo(entityTypeInfo.entityType, entityTypeMap)
    lazy val detailInfo = TargetedEntityTypeViewInfo(entityInfo, DetailUI)
    lazy val summaryInfo = TargetedEntityTypeViewInfo(entityInfo, SummaryUI)
    lazy val selectInfo = TargetedEntityTypeViewInfo(entityInfo, SelectUI)
    lazy val editInfo = TargetedEntityTypeViewInfo(entityInfo, EditUI, "edit_")
    val layoutPrefix = entityInfo.layoutPrefix
    if (entityTypeMap.isListable(entityTypeInfo.entityType)) {
      writeLayoutFile(layoutPrefix + "_list", listLayout(entityTypeInfo, childTypeInfos))
      writeLayoutFile(layoutPrefix + "_header", headerLayout(entityTypeInfo))
      writeLayoutFile(layoutPrefix + "_row", rowLayout(summaryInfo.viewIdFieldInfos))
    }
    if (pickedEntityTypes.contains(entityTypeInfo.entityType)) {
      writeLayoutFileIfNotEmpty(layoutPrefix + "_pick", pickLayout(selectInfo.viewIdFieldInfos))
    }
    if (!editInfo.isEmpty) writeLayoutFile(layoutPrefix + "_entry", entryLayout(editInfo.viewIdFieldInfos))
  }
}

object CrudUIGenerator extends CrudUIGenerator(overwrite = false) {
  def main(args: Array[String]) {
    val entityNavigationClassName = args(0)
    val constructor = Class.forName(entityNavigationClassName).getConstructor(classOf[PlatformDriver])
    val entityNavigation: EntityNavigation = constructor.newInstance(StubPlatformDriver).asInstanceOf[EntityNavigation]
    generateLayouts(entityNavigation.entityTypeMap, entityNavigation.getClass.getPackage.getName + ".android", "Android" + entityNavigation.applicationName.toTitleCase, classOf[CrudBackupAgent].getName)
  }
}
