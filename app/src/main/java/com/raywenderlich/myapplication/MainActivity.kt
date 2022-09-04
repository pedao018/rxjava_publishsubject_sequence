package com.raywenderlich.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.raywenderlich.myapplication.databinding.ActivityMainBinding
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers

import io.reactivex.rxjava3.subjects.PublishSubject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val publishSubjectOne = PublishSubject.create<ProgressNum>()
    val publishSubjectTwo = PublishSubject.create<ProgressNum>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createPublishSubject()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.clickOne.setOnClickListener { runSubject() }
    }

    private fun createPublishSubject() {
        publishSubjectOne
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribeBy(
                onNext = {
                    Log.e(
                        "hahaha",
                        "Next One: $it - Thread: ${Thread.currentThread().name}"
                    )
                    runOneFunction(it.result, it.waitTime)
                    publishSubjectTwo.onNext(it)
                },
                onComplete = {
                    Log.e(
                        "hahaha",
                        "Complete One - Thread: ${Thread.currentThread().name}"
                    )
                }
            )

        publishSubjectTwo
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doAfterNext {
                if (publishSubjectTwo.hasObservers()) {
                    Log.e("hahaha", "Pub Two has Observer- Thread: ${Thread.currentThread().name}")
                    if (publishSubjectOne.hasComplete() && it.result == 1)
                        publishSubjectTwo.onComplete()
                }
            }
            .subscribeBy(
                onNext = {
                    Log.e(
                        "hahaha",
                        "Next Two: $it - Thread: ${Thread.currentThread().name}"
                    )
                    runTwoFunction(it.result, it.waitTime)
                },
                onComplete = {
                    Log.e(
                        "hahaha",
                        "Complete Two - Thread: ${Thread.currentThread().name}"
                    )
                }
            )
    }

    private fun runSubject() {
        publishSubjectOne.onNext(ProgressNum(1, 3000))
        publishSubjectOne.onNext(ProgressNum(2, 5000))
        publishSubjectOne.onNext(ProgressNum(3, 4000))
        publishSubjectOne.onComplete()
    }

    private fun runOneFunction(result: Int, waitTime: Long): Int {
        Log.e("hahaha", "One Func Result: $result - Thread: ${Thread.currentThread().name}")
        Thread.sleep(waitTime)
        return result
    }

    private fun runTwoFunction(result: Int, waitTime: Long): Int {
        Log.e("hahaha", "Two Func Result: $result - Thread: ${Thread.currentThread().name}")
        Thread.sleep(waitTime)
        Log.e("hahaha", "Two Complete Result: $result - Thread: ${Thread.currentThread().name}")
        return result
    }
}

data class ProgressNum(val result: Int, val waitTime: Long)