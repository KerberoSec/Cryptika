package com.cryptika.messenger.data.local.db;

import android.content.Context;
import androidx.room.*;
import androidx.sqlite.db.SupportSQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\n\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\"\u0011\u0010\u0000\u001a\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0003\"\u0011\u0010\u0004\u001a\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0003\u00a8\u0006\u0006"}, d2 = {"MIGRATION_1_2", "Landroidx/room/migration/Migration;", "getMIGRATION_1_2", "()Landroidx/room/migration/Migration;", "MIGRATION_2_3", "getMIGRATION_2_3", "Cryptika_release"})
public final class AppDatabaseKt {
    
    /**
     * v1 → v2: Added `messageState` column to track delivery status (SENT/FAILED/DELIVERED).
     * All existing messages default to 'SENT' so historical data is preserved.
     */
    @org.jetbrains.annotations.NotNull()
    private static final androidx.room.migration.Migration MIGRATION_1_2 = null;
    
    /**
     * v2 → v3: Added `storageHashHex` column — SHA-256(ciphertextBlob) stored at write time
     * and recomputed on every read to detect DB corruption or tampering (PPT slide 12/13).
     * Existing messages default to '' (empty); they are treated as unverified until re-saved.
     */
    @org.jetbrains.annotations.NotNull()
    private static final androidx.room.migration.Migration MIGRATION_2_3 = null;
    
    /**
     * v1 → v2: Added `messageState` column to track delivery status (SENT/FAILED/DELIVERED).
     * All existing messages default to 'SENT' so historical data is preserved.
     */
    @org.jetbrains.annotations.NotNull()
    public static final androidx.room.migration.Migration getMIGRATION_1_2() {
        return null;
    }
    
    /**
     * v2 → v3: Added `storageHashHex` column — SHA-256(ciphertextBlob) stored at write time
     * and recomputed on every read to detect DB corruption or tampering (PPT slide 12/13).
     * Existing messages default to '' (empty); they are treated as unverified until re-saved.
     */
    @org.jetbrains.annotations.NotNull()
    public static final androidx.room.migration.Migration getMIGRATION_2_3() {
        return null;
    }
}