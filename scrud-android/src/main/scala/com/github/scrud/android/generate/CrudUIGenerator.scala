package com.github.scrud.android.generate

import com.github.triangle._
import collection.immutable.List
import xml._
import com.github.scrud.android.view.EntityView
import com.github.scrud.{CrudApplication, EntityType}
import com.github.scrud.util.FileConversions._
import java.io.File
import com.github.scrud.util.{Path, Common}
import com.github.scrud.android.{AndroidPlatformDriver, CrudAndroidApplication}

/** A UI Generator for a CrudTypes.
  * @author Eric Pabst (epabst@gmail.com)
  */

class CrudUIGenerator extends Logging {
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
    Option(file.getParentFile).foreach(_.mkdirs())
    file.writeAll("""<?xml version="1.0" encoding="utf-8"?>""", lineSeparator, prettyPrinter.format(xml))
    println("Wrote " + file)
  }

  def generateAndroidManifest(application: CrudApplication, androidApplicationClass: Class[_]): Elem = {
    if (!classOf[CrudAndroidApplication].isAssignableFrom(androidApplicationClass)) {
      throw new IllegalArgumentException(androidApplicationClass + " does not extend CrudAndroidApplication")
    }
    val activityNames = Seq(androidPlatformDriverFor(application).activityClass.getName)
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
              package={application.packageName}>
      <application android:label="@string/app_name" android:icon="@drawable/icon"
                   android:name={androidApplicationClass.getName}
                   android:theme="@android:style/Theme.NoTitleBar"
                   android:debuggable="true"
                   android:backupAgent={application.classNamePrefix + "BackupAgent"} android:restoreAnyVersion="true">
        <meta-data android:name="com.google.android.backup.api_key"
                   android:value="TODO: get a backup key from http://code.google.com/android/backup/signup.html and put it here."/>
        <activity android:name={activityNames.head} android:label="@string/app_name">
          <intent-filter>
            <action android:name="android.intent.action.MAIN"/>
            <category android:name="android.intent.category.LAUNCHER"/>
          </intent-filter>
        </activity>
        {activityNames.tail.map { name => <activity android:name={name} android:label="@string/app_name"/>}}
      </application>
      <uses-sdk android:minSdkVersion="8"/>
    </manifest>
  }

  private def androidPlatformDriverFor(application: CrudApplication): AndroidPlatformDriver = {
    application.platformDriver.asInstanceOf[AndroidPlatformDriver]
  }

  def generateValueStrings(entityInfo: EntityTypeViewInfo): NodeSeq = {
    import entityInfo._
    val addSeq = if (application.isCreatable(entityType)) <string name={"add_" + layoutPrefix}>Add {entityName}</string> else NodeSeq.Empty
    val editSeq = if (application.isSavable(entityType)) <string name={"edit_" + layoutPrefix}>Edit {entityName}</string> else NodeSeq.Empty
    val listSeq = if (application.isListable(entityType)) <string name={layoutPrefix + "_list"}>{entityName} List</string> else NodeSeq.Empty
    listSeq ++ addSeq ++ editSeq
  }

  def attemptToEvaluate[T](f: => T): Option[T] =
    try {
      Some(f)
    } catch {
      case e: Throwable => debug(e.toString); None
    }

  def generateValueStrings(application: CrudApplication): Elem = {
    <resources>
      <string name="app_name">{application.name}</string>
      {application.allEntityTypes.flatMap(entityType => generateValueStrings(EntityTypeViewInfo(entityType, application)))}
    </resources>
  }

  def generateLayouts(application: CrudApplication, androidApplicationClass: Class[_]) {
    val entityTypeInfos = application.allEntityTypes.map(EntityTypeViewInfo(_, application))
    val pickedEntityTypes: Seq[EntityType] = application.allEntityTypes.flatMap(_.deepCollect {
      case EntityView(pickedEntityName) => application.entityType(pickedEntityName)
    })
    entityTypeInfos.foreach(entityInfo => {
      val childViewInfos = application.childEntityTypes(entityInfo.entityType).map(EntityTypeViewInfo(_, application))
      generateLayouts(entityInfo, childViewInfos, application, pickedEntityTypes)
    })
    writeXmlToFile(Path("AndroidManifest.xml"), generateAndroidManifest(application, androidApplicationClass))
    writeXmlToFile(Path("res") / "values" / "strings.xml", generateValueStrings(application))
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

  protected[generate] def fieldLayoutForRow(field: ViewIdFieldInfo, position: Int): NodeSeq = {
    val textAppearance = if (position < 2) "?android:attr/textAppearanceLarge" else "?android:attr/textAppearanceSmall"
    val gravity = if (position % 2 == 0) "left" else "right"
    val layoutWidth = if (position % 2 == 0) "wrap_content" else "fill_parent"
    val attributes = <TextView android:id={"@+id/" + field.id} android:gravity={gravity}
                               android:layout_width={layoutWidth}
                               android:layout_height="wrap_content"
                               android:paddingRight="3sp"
                               android:textAppearance={textAppearance} style="@android:style/TextAppearance.Widget.TextView"/>.attributes
    adjustHeadNode(field.layout.displayXml, applyAttributes(_, attributes))
  }

  protected def emptyListRenderedDifferently: Boolean = false

  protected def listLayout(entityInfo: EntityTypeViewInfo, childEntityInfos: Seq[EntityTypeViewInfo], application: CrudApplication) = {
    val addableEntityTypeInfos = if (application.isCreatable(entityInfo.entityType)) List(entityInfo) else childEntityInfos.filter(childInfo => application.isCreatable(childInfo.entityType))
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                  android:orientation="vertical"
                  android:layout_width="fill_parent"
                  android:layout_height="fill_parent">
      <ListView android:id={"@+id/" + entityInfo.entityName + "_list"}
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_weight="1.0"/>
      {if (emptyListRenderedDifferently || addableEntityTypeInfos.isEmpty)
        <TextView android:id={"@+id/" + entityInfo.entityName + "_emptyList"}
                  android:layout_width="wrap_content"
                  android:layout_height="wrap_content" android:layout_weight="1"
                  android:text={"Empty " + entityInfo.entityName + " List"}
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

  protected def headerLayout(fields: List[ViewIdFieldInfo]) =
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                  android:paddingTop="2dip"
                  android:paddingBottom="2dip"
                  android:paddingLeft="6dip"
                  android:paddingRight="6dip"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:orientation="vertical">{
      fields.grouped(2).map { rowFields =>
        <LinearLayout android:layout_width="match_parent"
                      android:layout_height="wrap_content"
                      android:orientation="horizontal">
          {rowFields.map(field => fieldLayoutForHeader(field, fields.indexOf(field)))}
        </LinearLayout>
      }
    }
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
    fields.headOption.map { field =>
      val layout = fieldLayoutForRow(field, 0)
      val adjusted = adjustHeadNode(layout, _ match {
        case elem: Elem => elem.copy(scope = androidScope)
        case node => node
      })
      adjusted
    }.getOrElse(NodeSeq.Empty)
  }

  private def applyAttributes(xml: Node, attributes: MetaData): Node = xml match {
    case e: Elem => e % attributes
    case x => x
  }

  private def adjustHeadNode(xml: NodeSeq, f: Node => Node): NodeSeq = xml.headOption.map(f(_) +: xml.tail).getOrElse(xml)

  protected def fieldLayoutForEntry(field: ViewIdFieldInfo, position: Int): NodeSeq = {
    val textAppearance = "?android:attr/textAppearanceLarge"
    val attributes = <EditText android:id={"@+id/" + field.id} android:layout_width="fill_parent" android:layout_height="wrap_content"/>.attributes
    <result>
      <TextView android:text={field.displayName + ":"} android:textAppearance={textAppearance} style="@android:style/TextAppearance.Widget.TextView" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
      {adjustHeadNode(field.layout.editXml, applyAttributes(_, attributes))}
    </result>.child
  }

  def entryLayout(fields: List[ViewIdFieldInfo]): Elem = {
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                 android:orientation="vertical"
                 android:layout_width="fill_parent"
                 android:layout_height="wrap_content">
      {fields.flatMap(field => fieldLayoutForEntry(field, fields.indexOf(field)))}
    </LinearLayout>
  }

  private def writeLayoutFile(name: String, xml: Elem) {
    writeXmlToFile(Path("res") / "layout" / (name + ".xml"), xml)
  }

  private def writeLayoutFileIfNotEmpty(name: String, xml: NodeSeq) {
    xml match {
      case NodeSeq.Empty => // Don't write the file
      case Seq(headNode) => headNode match {
        case headElem: Elem => writeLayoutFile(name, headElem)
      }
    }
  }

  def generateLayouts(entityTypeInfo: EntityTypeViewInfo, childTypeInfos: Seq[EntityTypeViewInfo],
                      application: CrudApplication, pickedEntityTypes: Seq[EntityType]) {
    println("Generating layout for " + entityTypeInfo.entityType)
    lazy val info = EntityTypeViewInfo(entityTypeInfo.entityType, application)
    val layoutPrefix = info.layoutPrefix
    if (application.isListable(entityTypeInfo.entityType)) {
      writeLayoutFile(layoutPrefix + "_list", listLayout(entityTypeInfo, childTypeInfos, application))
      writeLayoutFile(layoutPrefix + "_header", headerLayout(info.displayableViewIdFieldInfos))
      writeLayoutFile(layoutPrefix + "_row", rowLayout(info.displayableViewIdFieldInfos))
    }
    if (pickedEntityTypes.contains(entityTypeInfo.entityType)) {
      writeLayoutFileIfNotEmpty(layoutPrefix + "_pick", pickLayout(info.displayableViewIdFieldInfos))
    }
    if (info.isUpdateable) writeLayoutFile(layoutPrefix + "_entry", entryLayout(info.updateableViewIdFieldInfos))
  }
}

object CrudUIGenerator extends CrudUIGenerator
