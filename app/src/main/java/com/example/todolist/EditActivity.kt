package com.example.todolist

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_edit.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.calendarView
import org.jetbrains.anko.yesButton
import java.util.*

class EditActivity : AppCompatActivity() {

    val realm = Realm.getDefaultInstance()
    val calendar : Calendar = Calendar.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        //업데이트 조건
        val id = intent.getLongExtra("id",-1L)
        if(id == -1L){
            insertMode()
        }
        else{
            updateMode(id)
        }

        //캘린더 뷰의 날짜를 선택했을 때 Caleandar 객체에 설정
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR,year)
            calendar.set(Calendar.MONTH,month)
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
        }
    }

    private fun insertTodo(){
        realm.beginTransaction() // 트래잭션 시작

        val newItem = realm.createObject<Todo>(nextId())//새 객체 생성
        newItem.title = todoEditText.text.toString()
        newItem.date = calendar.timeInMillis

        realm.commitTransaction() // 트랜잭션 종료 반영

        //다이얼로그 표시
        alert("내용이 추가되었습니다."){
            yesButton { finish() }
        }.show()
    }

    private fun nextId(): Int {
        val maxId = realm.where<Todo>().max("id")
        if(maxId != null){
            return maxId.toInt() + 1
        }
        return 0
    }

    private fun updateTodo(id : Long) {
        realm.beginTransaction()

        val updateItem = realm.where<Todo>().equalTo("id", id).findFirst()!!

        //값수정
        updateItem.title = todoEditText.text.toString()
        updateItem.date = calendar.timeInMillis

        realm.commitTransaction()

        //다이얼로그 표기
        alert("내용이 변경되었습니다.") {
            yesButton { finish() }
        }.show()
    }

    private fun deleteTodo(id : Long){
        realm.beginTransaction()
        val deleteItem = realm.where<Todo>().equalTo("id",id).findFirst()!!
        //삭제할 객체
        deleteItem.deleteFromRealm() // 삭제
        realm.commitTransaction()

        alert("내용이 삭제 되었습니다."){
            yesButton { finish() }
        }.show()
    }

    //추가 모드 초기화
    private fun insertMode(){
        //삭제 버튼을 감추기
        deleteFab.hide()

        //완료 버튼 클릭하면 추가
        doneFab.setOnClickListener {
            insertTodo()
        }
    }

    //수정 모드 초기화
    private fun updateMode(id : Long){
        //id에 해당하는 객체를 화면에 표시
        val todo = realm.where<Todo>().equalTo("id",id).findFirst()!!
        todoEditText.setText(todo.title)
        calendarView.date = todo.date

        //완료 버튼을 클릭하면 수정
        doneFab.setOnClickListener {
            updateTodo(id)
        }

        //삭제 버튼을 클릭하면 삭제
        deleteFab.setOnClickListener {
            deleteTodo(id)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
