package com.nevexis.backend

import org.jooq.UpdatableRecord
import java.math.BigDecimal

fun <T : UpdatableRecord<T>?> UpdatableRecord<T>.nullIfPrimaryKeyIsNull() =
    this.takeIf { it.key().fields().none { field -> field.getValue(this) == null } }


/**
 * Compares this record to another by matching their primary key
 * @return Returns 0 if all the values of this record are equal to all the values of the another record,
 * -1 if their primary keys are different,
 * 1 if their non-primary fields are different.
 */
fun <T : UpdatableRecord<T>> UpdatableRecord<T>.compareToByMatchingPk(record: UpdatableRecord<T>): Int {
    return if (this.key().fields().any { keyField ->
            !this[keyField].recordFieldValueEquals(record[keyField])
        }) {
        -1
    } else if (this.fields().toSet().minus(this.key().fields().toSet()).any { field ->
            !this[field].recordFieldValueEquals(record[field])
        }) {
        1
    } else {
        0
    }
}

private fun <T> T.recordFieldValueEquals(value: T) = when (value) {
    is BigDecimal -> (this as? BigDecimal)?.compareTo(value as? BigDecimal) == 0
    is ByteArray -> (this as? ByteArray).contentEquals(value as? ByteArray)
    else -> this == value
}