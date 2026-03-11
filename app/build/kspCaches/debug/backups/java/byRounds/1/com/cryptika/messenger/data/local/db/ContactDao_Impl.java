package com.cryptika.messenger.data.local.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ContactDao_Impl implements ContactDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ContactEntity> __insertionAdapterOfContactEntity;

  private final EntityDeletionOrUpdateAdapter<ContactEntity> __updateAdapterOfContactEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteContact;

  public ContactDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfContactEntity = new EntityInsertionAdapter<ContactEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `contacts` (`id`,`identityHashHex`,`publicKeyBase64`,`displayName`,`verifiedAt`,`keyChangedAt`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ContactEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getIdentityHashHex());
        statement.bindString(3, entity.getPublicKeyBase64());
        statement.bindString(4, entity.getDisplayName());
        statement.bindLong(5, entity.getVerifiedAt());
        if (entity.getKeyChangedAt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getKeyChangedAt());
        }
      }
    };
    this.__updateAdapterOfContactEntity = new EntityDeletionOrUpdateAdapter<ContactEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `contacts` SET `id` = ?,`identityHashHex` = ?,`publicKeyBase64` = ?,`displayName` = ?,`verifiedAt` = ?,`keyChangedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ContactEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getIdentityHashHex());
        statement.bindString(3, entity.getPublicKeyBase64());
        statement.bindString(4, entity.getDisplayName());
        statement.bindLong(5, entity.getVerifiedAt());
        if (entity.getKeyChangedAt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getKeyChangedAt());
        }
        statement.bindString(7, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteContact = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM contacts WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertContact(final ContactEntity contact,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfContactEntity.insert(contact);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateContact(final ContactEntity contact,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfContactEntity.handle(contact);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteContact(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteContact.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteContact.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ContactEntity>> getAllContacts() {
    final String _sql = "SELECT * FROM contacts ORDER BY displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"contacts"}, new Callable<List<ContactEntity>>() {
      @Override
      @NonNull
      public List<ContactEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIdentityHashHex = CursorUtil.getColumnIndexOrThrow(_cursor, "identityHashHex");
          final int _cursorIndexOfPublicKeyBase64 = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKeyBase64");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfVerifiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "verifiedAt");
          final int _cursorIndexOfKeyChangedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "keyChangedAt");
          final List<ContactEntity> _result = new ArrayList<ContactEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ContactEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpIdentityHashHex;
            _tmpIdentityHashHex = _cursor.getString(_cursorIndexOfIdentityHashHex);
            final String _tmpPublicKeyBase64;
            _tmpPublicKeyBase64 = _cursor.getString(_cursorIndexOfPublicKeyBase64);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final long _tmpVerifiedAt;
            _tmpVerifiedAt = _cursor.getLong(_cursorIndexOfVerifiedAt);
            final Long _tmpKeyChangedAt;
            if (_cursor.isNull(_cursorIndexOfKeyChangedAt)) {
              _tmpKeyChangedAt = null;
            } else {
              _tmpKeyChangedAt = _cursor.getLong(_cursorIndexOfKeyChangedAt);
            }
            _item = new ContactEntity(_tmpId,_tmpIdentityHashHex,_tmpPublicKeyBase64,_tmpDisplayName,_tmpVerifiedAt,_tmpKeyChangedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getContact(final String id, final Continuation<? super ContactEntity> $completion) {
    final String _sql = "SELECT * FROM contacts WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ContactEntity>() {
      @Override
      @Nullable
      public ContactEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIdentityHashHex = CursorUtil.getColumnIndexOrThrow(_cursor, "identityHashHex");
          final int _cursorIndexOfPublicKeyBase64 = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKeyBase64");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfVerifiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "verifiedAt");
          final int _cursorIndexOfKeyChangedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "keyChangedAt");
          final ContactEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpIdentityHashHex;
            _tmpIdentityHashHex = _cursor.getString(_cursorIndexOfIdentityHashHex);
            final String _tmpPublicKeyBase64;
            _tmpPublicKeyBase64 = _cursor.getString(_cursorIndexOfPublicKeyBase64);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final long _tmpVerifiedAt;
            _tmpVerifiedAt = _cursor.getLong(_cursorIndexOfVerifiedAt);
            final Long _tmpKeyChangedAt;
            if (_cursor.isNull(_cursorIndexOfKeyChangedAt)) {
              _tmpKeyChangedAt = null;
            } else {
              _tmpKeyChangedAt = _cursor.getLong(_cursorIndexOfKeyChangedAt);
            }
            _result = new ContactEntity(_tmpId,_tmpIdentityHashHex,_tmpPublicKeyBase64,_tmpDisplayName,_tmpVerifiedAt,_tmpKeyChangedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getContactByIdentityHash(final String hex,
      final Continuation<? super ContactEntity> $completion) {
    final String _sql = "SELECT * FROM contacts WHERE identityHashHex = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, hex);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ContactEntity>() {
      @Override
      @Nullable
      public ContactEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIdentityHashHex = CursorUtil.getColumnIndexOrThrow(_cursor, "identityHashHex");
          final int _cursorIndexOfPublicKeyBase64 = CursorUtil.getColumnIndexOrThrow(_cursor, "publicKeyBase64");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfVerifiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "verifiedAt");
          final int _cursorIndexOfKeyChangedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "keyChangedAt");
          final ContactEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpIdentityHashHex;
            _tmpIdentityHashHex = _cursor.getString(_cursorIndexOfIdentityHashHex);
            final String _tmpPublicKeyBase64;
            _tmpPublicKeyBase64 = _cursor.getString(_cursorIndexOfPublicKeyBase64);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final long _tmpVerifiedAt;
            _tmpVerifiedAt = _cursor.getLong(_cursorIndexOfVerifiedAt);
            final Long _tmpKeyChangedAt;
            if (_cursor.isNull(_cursorIndexOfKeyChangedAt)) {
              _tmpKeyChangedAt = null;
            } else {
              _tmpKeyChangedAt = _cursor.getLong(_cursorIndexOfKeyChangedAt);
            }
            _result = new ContactEntity(_tmpId,_tmpIdentityHashHex,_tmpPublicKeyBase64,_tmpDisplayName,_tmpVerifiedAt,_tmpKeyChangedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
