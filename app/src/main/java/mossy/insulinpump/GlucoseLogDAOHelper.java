package mossy.insulinpump;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class GlucoseLogDAOHelper extends SQLiteOpenHelper {
    private static final int VERSION = 7;
    private final Context context;

    GlucoseLogDAOHelper(Context context) {
        super(context, "glucose_log.db", null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(context.getString(R.string.create_insulin_log_table, "glucose_log", "time", "amount"));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(context.getString(R.string.delete_table, "glucose_log"));
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
