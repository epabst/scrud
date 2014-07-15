package com.github.scrud.android.persistence

import com.github.scrud.persistence.EntityTypeMapForTesting
import com.github.scrud.android.{EntityForTesting2, EntityTypeForTesting}
import com.github.scrud.android.backup.EntityTypeMapWithBackup

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 7/14/14
 */
class EntityTypeMapWithBackupForTesting extends EntityTypeMapForTesting(
    EntityTypeForTesting, new EntityTypeForTesting(EntityForTesting2)) with EntityTypeMapWithBackup

object EntityTypeMapWithBackupForTesting extends EntityTypeMapWithBackupForTesting
