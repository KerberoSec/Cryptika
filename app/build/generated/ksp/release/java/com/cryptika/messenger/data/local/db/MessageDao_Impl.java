package com.cryptika.messenger.data.local.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
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
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MessageEntity> __insertionAdapterOfMessageEntity;

  private final SharedSQLiteStatement __preparedStmtOfZeroizeAndMarkUnrecoverable;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessage;

  private final SharedSQLiteStatement __preparedStmtOfDeleteConversationMessages;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageState;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMessageEntity = new EntityInsertionAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `messages` (`id`,`conversationId`,`senderIdHex`,`ciphertextBlob`,`storageKeyAlias`,`storageHashHex`,`headerJson`,`timestampMs`,`counter`,`expiryMs`,`isDecryptable`,`isOutgoing`,`messageType`,`messageState`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getConversationId());
        statement.bindString(3, entity.getSenderIdHex());
        statement.bindBlob(4, entity.getCiphertextBlob());
        statement.bindString(5, entity.getStorageKeyAlias());
        statement.bindString(6, entity.getStorageHashHex());
        statement.bindString(7, entity.getHeaderJson());
        statement.bindLong(8, entity.getTimestampMs());
        statement.bindLong(9, entity.getCounter());
        if (entity.getExpiryMs() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getExpiryMs());
        }
        final int _tmp = entity.isDecryptable() ? 1 : 0;
        statement.bindLong(11, _tmp);
        final int _tmp_1 = entity.isOutgoing() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        statement.bindString(13, entity.getMessageType());
        statement.bindString(14, entity.getMessageState());
      }
    };
    this.__preparedStmtOfZeroizeAndMarkUnrecoverable = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET ciphertextBlob = x'00', isDecryptable = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMessage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteConversationMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE conversationId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateMessageState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET messageState = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMessageEntity.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object zeroizeAndMarkUnrecoverable(final String id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfZeroizeAndMarkUnrecoverable.acquire();
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
          __preparedStmtOfZeroizeAndMarkUnrecoverable.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessage(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessage.acquire();
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
          __preparedStmtOfDeleteMessage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteConversationMessages(final String conversationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteConversationMessages.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, conversationId);
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
          __preparedStmtOfDeleteConversationMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessageState(final String id, final String state,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMessageState.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, state);
        _argIndex = 2;
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
          __preparedStmtOfUpdateMessageState.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MessageEntity>> getMessages(final String conversationId) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestampMs ASC, rowid ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, conversationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderIdHex = CursorUtil.getColumnIndexOrThrow(_cursor, "senderIdHex");
          final int _cursorIndexOfCiphertextBlob = CursorUtil.getColumnIndexOrThrow(_cursor, "ciphertextBlob");
          final int _cursorIndexOfStorageKeyAlias = CursorUtil.getColumnIndexOrThrow(_cursor, "storageKeyAlias");
          final int _cursorIndexOfStorageHashHex = CursorUtil.getColumnIndexOrThrow(_cursor, "storageHashHex");
          final int _cursorIndexOfHeaderJson = CursorUtil.getColumnIndexOrThrow(_cursor, "headerJson");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfCounter = CursorUtil.getColumnIndexOrThrow(_cursor, "counter");
          final int _cursorIndexOfExpiryMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryMs");
          final int _cursorIndexOfIsDecryptable = CursorUtil.getColumnIndexOrThrow(_cursor, "isDecryptable");
          final int _cursorIndexOfIsOutgoing = CursorUtil.getColumnIndexOrThrow(_cursor, "isOutgoing");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfMessageState = CursorUtil.getColumnIndexOrThrow(_cursor, "messageState");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderIdHex;
            _tmpSenderIdHex = _cursor.getString(_cursorIndexOfSenderIdHex);
            final byte[] _tmpCiphertextBlob;
            _tmpCiphertextBlob = _cursor.getBlob(_cursorIndexOfCiphertextBlob);
            final String _tmpStorageKeyAlias;
            _tmpStorageKeyAlias = _cursor.getString(_cursorIndexOfStorageKeyAlias);
            final String _tmpStorageHashHex;
            _tmpStorageHashHex = _cursor.getString(_cursorIndexOfStorageHashHex);
            final String _tmpHeaderJson;
            _tmpHeaderJson = _cursor.getString(_cursorIndexOfHeaderJson);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final long _tmpCounter;
            _tmpCounter = _cursor.getLong(_cursorIndexOfCounter);
            final Long _tmpExpiryMs;
            if (_cursor.isNull(_cursorIndexOfExpiryMs)) {
              _tmpExpiryMs = null;
            } else {
              _tmpExpiryMs = _cursor.getLong(_cursorIndexOfExpiryMs);
            }
            final boolean _tmpIsDecryptable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDecryptable);
            _tmpIsDecryptable = _tmp != 0;
            final boolean _tmpIsOutgoing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsOutgoing);
            _tmpIsOutgoing = _tmp_1 != 0;
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpMessageState;
            _tmpMessageState = _cursor.getString(_cursorIndexOfMessageState);
            _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderIdHex,_tmpCiphertextBlob,_tmpStorageKeyAlias,_tmpStorageHashHex,_tmpHeaderJson,_tmpTimestampMs,_tmpCounter,_tmpExpiryMs,_tmpIsDecryptable,_tmpIsOutgoing,_tmpMessageType,_tmpMessageState);
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
  public Object getMessage(final String id, final Continuation<? super MessageEntity> $completion) {
    final String _sql = "SELECT * FROM messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MessageEntity>() {
      @Override
      @Nullable
      public MessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderIdHex = CursorUtil.getColumnIndexOrThrow(_cursor, "senderIdHex");
          final int _cursorIndexOfCiphertextBlob = CursorUtil.getColumnIndexOrThrow(_cursor, "ciphertextBlob");
          final int _cursorIndexOfStorageKeyAlias = CursorUtil.getColumnIndexOrThrow(_cursor, "storageKeyAlias");
          final int _cursorIndexOfStorageHashHex = CursorUtil.getColumnIndexOrThrow(_cursor, "storageHashHex");
          final int _cursorIndexOfHeaderJson = CursorUtil.getColumnIndexOrThrow(_cursor, "headerJson");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfCounter = CursorUtil.getColumnIndexOrThrow(_cursor, "counter");
          final int _cursorIndexOfExpiryMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryMs");
          final int _cursorIndexOfIsDecryptable = CursorUtil.getColumnIndexOrThrow(_cursor, "isDecryptable");
          final int _cursorIndexOfIsOutgoing = CursorUtil.getColumnIndexOrThrow(_cursor, "isOutgoing");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfMessageState = CursorUtil.getColumnIndexOrThrow(_cursor, "messageState");
          final MessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderIdHex;
            _tmpSenderIdHex = _cursor.getString(_cursorIndexOfSenderIdHex);
            final byte[] _tmpCiphertextBlob;
            _tmpCiphertextBlob = _cursor.getBlob(_cursorIndexOfCiphertextBlob);
            final String _tmpStorageKeyAlias;
            _tmpStorageKeyAlias = _cursor.getString(_cursorIndexOfStorageKeyAlias);
            final String _tmpStorageHashHex;
            _tmpStorageHashHex = _cursor.getString(_cursorIndexOfStorageHashHex);
            final String _tmpHeaderJson;
            _tmpHeaderJson = _cursor.getString(_cursorIndexOfHeaderJson);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final long _tmpCounter;
            _tmpCounter = _cursor.getLong(_cursorIndexOfCounter);
            final Long _tmpExpiryMs;
            if (_cursor.isNull(_cursorIndexOfExpiryMs)) {
              _tmpExpiryMs = null;
            } else {
              _tmpExpiryMs = _cursor.getLong(_cursorIndexOfExpiryMs);
            }
            final boolean _tmpIsDecryptable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDecryptable);
            _tmpIsDecryptable = _tmp != 0;
            final boolean _tmpIsOutgoing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsOutgoing);
            _tmpIsOutgoing = _tmp_1 != 0;
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpMessageState;
            _tmpMessageState = _cursor.getString(_cursorIndexOfMessageState);
            _result = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderIdHex,_tmpCiphertextBlob,_tmpStorageKeyAlias,_tmpStorageHashHex,_tmpHeaderJson,_tmpTimestampMs,_tmpCounter,_tmpExpiryMs,_tmpIsDecryptable,_tmpIsOutgoing,_tmpMessageType,_tmpMessageState);
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
  public Object getMessageByCounterAndSender(final String convId, final String senderHex,
      final long counter, final Continuation<? super MessageEntity> $completion) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? AND senderIdHex = ? AND counter = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, convId);
    _argIndex = 2;
    _statement.bindString(_argIndex, senderHex);
    _argIndex = 3;
    _statement.bindLong(_argIndex, counter);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MessageEntity>() {
      @Override
      @Nullable
      public MessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderIdHex = CursorUtil.getColumnIndexOrThrow(_cursor, "senderIdHex");
          final int _cursorIndexOfCiphertextBlob = CursorUtil.getColumnIndexOrThrow(_cursor, "ciphertextBlob");
          final int _cursorIndexOfStorageKeyAlias = CursorUtil.getColumnIndexOrThrow(_cursor, "storageKeyAlias");
          final int _cursorIndexOfStorageHashHex = CursorUtil.getColumnIndexOrThrow(_cursor, "storageHashHex");
          final int _cursorIndexOfHeaderJson = CursorUtil.getColumnIndexOrThrow(_cursor, "headerJson");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfCounter = CursorUtil.getColumnIndexOrThrow(_cursor, "counter");
          final int _cursorIndexOfExpiryMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryMs");
          final int _cursorIndexOfIsDecryptable = CursorUtil.getColumnIndexOrThrow(_cursor, "isDecryptable");
          final int _cursorIndexOfIsOutgoing = CursorUtil.getColumnIndexOrThrow(_cursor, "isOutgoing");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfMessageState = CursorUtil.getColumnIndexOrThrow(_cursor, "messageState");
          final MessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderIdHex;
            _tmpSenderIdHex = _cursor.getString(_cursorIndexOfSenderIdHex);
            final byte[] _tmpCiphertextBlob;
            _tmpCiphertextBlob = _cursor.getBlob(_cursorIndexOfCiphertextBlob);
            final String _tmpStorageKeyAlias;
            _tmpStorageKeyAlias = _cursor.getString(_cursorIndexOfStorageKeyAlias);
            final String _tmpStorageHashHex;
            _tmpStorageHashHex = _cursor.getString(_cursorIndexOfStorageHashHex);
            final String _tmpHeaderJson;
            _tmpHeaderJson = _cursor.getString(_cursorIndexOfHeaderJson);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final long _tmpCounter;
            _tmpCounter = _cursor.getLong(_cursorIndexOfCounter);
            final Long _tmpExpiryMs;
            if (_cursor.isNull(_cursorIndexOfExpiryMs)) {
              _tmpExpiryMs = null;
            } else {
              _tmpExpiryMs = _cursor.getLong(_cursorIndexOfExpiryMs);
            }
            final boolean _tmpIsDecryptable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDecryptable);
            _tmpIsDecryptable = _tmp != 0;
            final boolean _tmpIsOutgoing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsOutgoing);
            _tmpIsOutgoing = _tmp_1 != 0;
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpMessageState;
            _tmpMessageState = _cursor.getString(_cursorIndexOfMessageState);
            _result = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderIdHex,_tmpCiphertextBlob,_tmpStorageKeyAlias,_tmpStorageHashHex,_tmpHeaderJson,_tmpTimestampMs,_tmpCounter,_tmpExpiryMs,_tmpIsDecryptable,_tmpIsOutgoing,_tmpMessageType,_tmpMessageState);
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
  public Object getExpiredMessages(final long nowMs,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE expiryMs IS NOT NULL AND expiryMs <= ? AND isDecryptable = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, nowMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderIdHex = CursorUtil.getColumnIndexOrThrow(_cursor, "senderIdHex");
          final int _cursorIndexOfCiphertextBlob = CursorUtil.getColumnIndexOrThrow(_cursor, "ciphertextBlob");
          final int _cursorIndexOfStorageKeyAlias = CursorUtil.getColumnIndexOrThrow(_cursor, "storageKeyAlias");
          final int _cursorIndexOfStorageHashHex = CursorUtil.getColumnIndexOrThrow(_cursor, "storageHashHex");
          final int _cursorIndexOfHeaderJson = CursorUtil.getColumnIndexOrThrow(_cursor, "headerJson");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfCounter = CursorUtil.getColumnIndexOrThrow(_cursor, "counter");
          final int _cursorIndexOfExpiryMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryMs");
          final int _cursorIndexOfIsDecryptable = CursorUtil.getColumnIndexOrThrow(_cursor, "isDecryptable");
          final int _cursorIndexOfIsOutgoing = CursorUtil.getColumnIndexOrThrow(_cursor, "isOutgoing");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfMessageState = CursorUtil.getColumnIndexOrThrow(_cursor, "messageState");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderIdHex;
            _tmpSenderIdHex = _cursor.getString(_cursorIndexOfSenderIdHex);
            final byte[] _tmpCiphertextBlob;
            _tmpCiphertextBlob = _cursor.getBlob(_cursorIndexOfCiphertextBlob);
            final String _tmpStorageKeyAlias;
            _tmpStorageKeyAlias = _cursor.getString(_cursorIndexOfStorageKeyAlias);
            final String _tmpStorageHashHex;
            _tmpStorageHashHex = _cursor.getString(_cursorIndexOfStorageHashHex);
            final String _tmpHeaderJson;
            _tmpHeaderJson = _cursor.getString(_cursorIndexOfHeaderJson);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final long _tmpCounter;
            _tmpCounter = _cursor.getLong(_cursorIndexOfCounter);
            final Long _tmpExpiryMs;
            if (_cursor.isNull(_cursorIndexOfExpiryMs)) {
              _tmpExpiryMs = null;
            } else {
              _tmpExpiryMs = _cursor.getLong(_cursorIndexOfExpiryMs);
            }
            final boolean _tmpIsDecryptable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDecryptable);
            _tmpIsDecryptable = _tmp != 0;
            final boolean _tmpIsOutgoing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsOutgoing);
            _tmpIsOutgoing = _tmp_1 != 0;
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpMessageState;
            _tmpMessageState = _cursor.getString(_cursorIndexOfMessageState);
            _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderIdHex,_tmpCiphertextBlob,_tmpStorageKeyAlias,_tmpStorageHashHex,_tmpHeaderJson,_tmpTimestampMs,_tmpCounter,_tmpExpiryMs,_tmpIsDecryptable,_tmpIsOutgoing,_tmpMessageType,_tmpMessageState);
            _result.add(_item);
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
  public Object getAllMessagesForConversation(final String conversationId,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, conversationId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderIdHex = CursorUtil.getColumnIndexOrThrow(_cursor, "senderIdHex");
          final int _cursorIndexOfCiphertextBlob = CursorUtil.getColumnIndexOrThrow(_cursor, "ciphertextBlob");
          final int _cursorIndexOfStorageKeyAlias = CursorUtil.getColumnIndexOrThrow(_cursor, "storageKeyAlias");
          final int _cursorIndexOfStorageHashHex = CursorUtil.getColumnIndexOrThrow(_cursor, "storageHashHex");
          final int _cursorIndexOfHeaderJson = CursorUtil.getColumnIndexOrThrow(_cursor, "headerJson");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfCounter = CursorUtil.getColumnIndexOrThrow(_cursor, "counter");
          final int _cursorIndexOfExpiryMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryMs");
          final int _cursorIndexOfIsDecryptable = CursorUtil.getColumnIndexOrThrow(_cursor, "isDecryptable");
          final int _cursorIndexOfIsOutgoing = CursorUtil.getColumnIndexOrThrow(_cursor, "isOutgoing");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfMessageState = CursorUtil.getColumnIndexOrThrow(_cursor, "messageState");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderIdHex;
            _tmpSenderIdHex = _cursor.getString(_cursorIndexOfSenderIdHex);
            final byte[] _tmpCiphertextBlob;
            _tmpCiphertextBlob = _cursor.getBlob(_cursorIndexOfCiphertextBlob);
            final String _tmpStorageKeyAlias;
            _tmpStorageKeyAlias = _cursor.getString(_cursorIndexOfStorageKeyAlias);
            final String _tmpStorageHashHex;
            _tmpStorageHashHex = _cursor.getString(_cursorIndexOfStorageHashHex);
            final String _tmpHeaderJson;
            _tmpHeaderJson = _cursor.getString(_cursorIndexOfHeaderJson);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final long _tmpCounter;
            _tmpCounter = _cursor.getLong(_cursorIndexOfCounter);
            final Long _tmpExpiryMs;
            if (_cursor.isNull(_cursorIndexOfExpiryMs)) {
              _tmpExpiryMs = null;
            } else {
              _tmpExpiryMs = _cursor.getLong(_cursorIndexOfExpiryMs);
            }
            final boolean _tmpIsDecryptable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDecryptable);
            _tmpIsDecryptable = _tmp != 0;
            final boolean _tmpIsOutgoing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsOutgoing);
            _tmpIsOutgoing = _tmp_1 != 0;
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpMessageState;
            _tmpMessageState = _cursor.getString(_cursorIndexOfMessageState);
            _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderIdHex,_tmpCiphertextBlob,_tmpStorageKeyAlias,_tmpStorageHashHex,_tmpHeaderJson,_tmpTimestampMs,_tmpCounter,_tmpExpiryMs,_tmpIsDecryptable,_tmpIsOutgoing,_tmpMessageType,_tmpMessageState);
            _result.add(_item);
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
  public Object getMaxCounter(final String conversationId, final String senderHex,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT MAX(counter) FROM messages WHERE conversationId = ? AND senderIdHex = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, conversationId);
    _argIndex = 2;
    _statement.bindString(_argIndex, senderHex);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
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
  public Object getFailedMessages(final String conversationId,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? AND isOutgoing = 1 AND messageState = 'FAILED' ORDER BY counter ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, conversationId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderIdHex = CursorUtil.getColumnIndexOrThrow(_cursor, "senderIdHex");
          final int _cursorIndexOfCiphertextBlob = CursorUtil.getColumnIndexOrThrow(_cursor, "ciphertextBlob");
          final int _cursorIndexOfStorageKeyAlias = CursorUtil.getColumnIndexOrThrow(_cursor, "storageKeyAlias");
          final int _cursorIndexOfStorageHashHex = CursorUtil.getColumnIndexOrThrow(_cursor, "storageHashHex");
          final int _cursorIndexOfHeaderJson = CursorUtil.getColumnIndexOrThrow(_cursor, "headerJson");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfCounter = CursorUtil.getColumnIndexOrThrow(_cursor, "counter");
          final int _cursorIndexOfExpiryMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryMs");
          final int _cursorIndexOfIsDecryptable = CursorUtil.getColumnIndexOrThrow(_cursor, "isDecryptable");
          final int _cursorIndexOfIsOutgoing = CursorUtil.getColumnIndexOrThrow(_cursor, "isOutgoing");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfMessageState = CursorUtil.getColumnIndexOrThrow(_cursor, "messageState");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderIdHex;
            _tmpSenderIdHex = _cursor.getString(_cursorIndexOfSenderIdHex);
            final byte[] _tmpCiphertextBlob;
            _tmpCiphertextBlob = _cursor.getBlob(_cursorIndexOfCiphertextBlob);
            final String _tmpStorageKeyAlias;
            _tmpStorageKeyAlias = _cursor.getString(_cursorIndexOfStorageKeyAlias);
            final String _tmpStorageHashHex;
            _tmpStorageHashHex = _cursor.getString(_cursorIndexOfStorageHashHex);
            final String _tmpHeaderJson;
            _tmpHeaderJson = _cursor.getString(_cursorIndexOfHeaderJson);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final long _tmpCounter;
            _tmpCounter = _cursor.getLong(_cursorIndexOfCounter);
            final Long _tmpExpiryMs;
            if (_cursor.isNull(_cursorIndexOfExpiryMs)) {
              _tmpExpiryMs = null;
            } else {
              _tmpExpiryMs = _cursor.getLong(_cursorIndexOfExpiryMs);
            }
            final boolean _tmpIsDecryptable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDecryptable);
            _tmpIsDecryptable = _tmp != 0;
            final boolean _tmpIsOutgoing;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsOutgoing);
            _tmpIsOutgoing = _tmp_1 != 0;
            final String _tmpMessageType;
            _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            final String _tmpMessageState;
            _tmpMessageState = _cursor.getString(_cursorIndexOfMessageState);
            _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderIdHex,_tmpCiphertextBlob,_tmpStorageKeyAlias,_tmpStorageHashHex,_tmpHeaderJson,_tmpTimestampMs,_tmpCounter,_tmpExpiryMs,_tmpIsDecryptable,_tmpIsOutgoing,_tmpMessageType,_tmpMessageState);
            _result.add(_item);
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
