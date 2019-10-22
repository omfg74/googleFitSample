package ru.omfgdevelop.googleapisample

import android.content.Context
import android.util.Log
import androidx.core.util.rangeTo
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.Session
import com.google.android.gms.fitness.request.SessionReadRequest
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList




class WalkWorker {
    fun fetchWalks(context: Context, walksCallBack: WalksCallBack) {
        var startTime = 0L
        val storage: StorageInterface = SharedWorker()
        val savedTime: Long = storage.getSavedTime(context)
        val walkList: MutableList<Walk> = ArrayList()
        if (savedTime != 0L) {
//            startTime = savedTime+1
        }
        Log.d("Log","start time "+startTime);
        val cal = Calendar.getInstance()
        val now = Calendar.getInstance()
        val endTime: Long
        cal.time = now.time
        endTime = cal.timeInMillis
        if (startTime == 0L) {
            cal.add(Calendar.DAY_OF_MONTH, -4)
            startTime = cal.timeInMillis
        }
        val readRequest: SessionReadRequest = SessionReadRequest.Builder()
            .read(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .read(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .read(DataType.AGGREGATE_MOVE_MINUTES)
            .enableServerQueries()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .readSessionsFromAllApps()
            .build()
        Fitness.getSessionsClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readSession(readRequest)
            .addOnSuccessListener { sessionReadResponse ->
                val sessions: MutableList<Session> = sessionReadResponse.sessions
                Log.d("Log","sessions size"+sessions.size)
                Log.d("Log","start time"+startTime/1000/60/60/24);
                Log.d("Log","end time"+endTime/1000/60/60/24)
                val calendar = Calendar.getInstance()
                calendar.setTimeInMillis(startTime)
                var mDay = calendar.get(Calendar.DAY_OF_MONTH)
                var mMonth = calendar.get(Calendar.MONTH)
                var mYear = calendar.get(Calendar.YEAR)
                Log.d("Log","start date"+mDay+" "+mMonth+" "+" "+mYear)
                calendar.setTimeInMillis(endTime)
                mDay = calendar.get(Calendar.DAY_OF_MONTH)
                mMonth = calendar.get(Calendar.MONTH)
                mYear = calendar.get(Calendar.YEAR)
                Log.d("Log","end date"+mDay+" "+mMonth+" "+" "+mYear)


                sessions.withIndex().forEach {
                    if (it.value.activity.equals("walking")) {
                        Log.d("Log", "name" + it.value.name)
                        Log.d("Log", "descr " + it.value.describeContents())
                        Log.d("Log", "act " + it.value.activity)
                        Log.d("Log", "ident " + it.value.identifier)
                        Log.d("Log", "ongoing " + it.value.isOngoing)
                        Log.d("Log", "start time  " + it.value.getStartTime(TimeUnit.MILLISECONDS))
                        Log.d("Log", "start time  days" + it.value.getStartTime(TimeUnit.DAYS))
                        Log.d("Log", "end time " + it.value.getEndTime(TimeUnit.MILLISECONDS))
                        Log.d("Log", "end time days" + it.value.getEndTime(TimeUnit.DAYS))
                        val walk = Walk()
                        val dataSets: MutableList<DataSet> =
                            sessionReadResponse.getDataSet(it.value)
                        dataSets.withIndex().forEach { dataSet ->
                            run {
                                if (dataSet.value.dataPoints.size > 0) {
                                    if (dataSet.value.dataType.fields.contains(Field.FIELD_STEPS)) {
                                        walk.steps = Integer.parseInt(
                                            dataSet.value.dataPoints[dataSet.value.dataPoints.size - 1].getValue(
                                                Field.FIELD_STEPS
                                            ).toString()
                                        )
                                        Log.d(
                                            "Log",
                                            "steps " + dataSet.value.dataPoints[dataSet.value.dataPoints.size - 1].getValue(
                                                Field.FIELD_STEPS
                                            )
                                        )
                                    }
                                    if (dataSet.value.dataType.fields.contains(Field.FIELD_DURATION)) {
                                        walk.duration = Integer.parseInt(
                                            dataSet.value.dataPoints[dataSet.value.dataPoints.size - 1].getValue(
                                                Field.FIELD_DURATION
                                            ).toString()
                                        ).toLong()
                                        Log.d(
                                            "Log",
                                            "Duration  " + dataSet.value.dataPoints[dataSet.value.dataPoints.size - 1].getValue(
                                                Field.FIELD_DURATION
                                            )
                                        )
                                    }
                                    walkList.add(walk)
                                }
                              walksCallBack.getWalks(walkList)

                            }

                        }
                    }
                }
                if (sessions.size > 0) {
                    storage.saveLastWalkEndTime(
                        context,
                        sessions.get(sessions.lastIndex).getEndTime(TimeUnit.MILLISECONDS)
                    )
                }

            }
            .addOnFailureListener {
                Log.d("appLog", "" + it)
            }


    }
}