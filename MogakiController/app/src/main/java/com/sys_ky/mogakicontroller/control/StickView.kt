package com.sys_ky.mogakicontroller.control

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

private interface StickMoveProperty {
    val MoveStickPercent_X: Float
    val MoveStickPercent_Y: Float
    val MoveTouchShift_X: Float
    val MoveTouchShift_Y: Float
}

class StickView constructor(context: Context?, attrs: AttributeSet?) : View(context, attrs), StickMoveProperty{

    //スティックとスティック可動エリアの比率
    private val c_stickRatio: Float = 3f
    private val c_stickInRatio: Float = 3.4f

    //ビュー全体の背景の幅・高さ
    private var rect_width: Float = 0f
    private var rect_height: Float = 0f
    //ビューの中心座標
    private var center_X: Float = 0f
    private var center_Y: Float = 0f
    //スティック可動エリアとスティック、スティック内の線の半径
    private var radius_Area: Float = 0f
    private var radius_Stick: Float = 0f
    private var radius_StorokeInStick: Float = 0f
    //スティックの最大移動距離（ビュー中心からスティック中心までの直線距離）
    private var maxShift: Float = 0f
    //現在のスティック中心座標
    private var nowCenter_X = 0f
    private var nowCenter_Y = 0f

    //ペイント
    private val Paint_rect: Paint  = Paint()
    private val Paint_Area: Paint = Paint()
    private val Paint_Stick: Paint = Paint()
    private val Paint_StickStroke: Paint = Paint()

    //初回描画判定用フラグ
    private var firstFlg = true

    //タッチしてスライドした移動量
    private var move_X: Float = 0f
    private var move_Y: Float = 0f

    //タッチ開始時の座標
    private var touchStartPos_X: Float = 0f
    private var touchStartPos_Y: Float = 0f

    //プロパティ
    override var MoveStickPercent_X: Float = 0.0f
        private set
    override var MoveStickPercent_Y: Float = 0.0f
        private set
    override var MoveTouchShift_X: Float = 0.0f
        private set
    override var MoveTouchShift_Y: Float = 0.0f
        private set

    init {
        //描画用ペイントを定義
        //背景用の四角
        Paint_rect.style = Paint.Style.FILL
        Paint_rect.color = Color.WHITE
        Paint_rect.alpha = 0
        //スティック可動エリアの円
        Paint_Area.color = Color.LTGRAY
        Paint_Area.style = Paint.Style.FILL
        //スティック用の円
        Paint_Stick.color = Color.DKGRAY
        Paint_Stick.style = Paint.Style.FILL
        //スティックの内側の線用の円
        Paint_StickStroke.color = Color.BLACK
        Paint_StickStroke.style = Paint.Style.STROKE
        Paint_StickStroke.strokeWidth = 8f
    }

    override fun onDraw(canvas: Canvas?) {
        //初期状態で中心座標など保存
        if(firstFlg) {
            rect_width = right.toFloat() - left.toFloat()
            rect_height = bottom.toFloat() - top.toFloat()
            center_X = (right.toFloat() - left.toFloat()) / 2f
            center_Y = (bottom.toFloat() - top.toFloat()) / 2f
            //幅と高さで小さい方に円の半径を合わせる
            if(rect_width >= rect_height) {
                radius_Area = (bottom.toFloat() - top.toFloat()) / 2f
                radius_Stick = (bottom.toFloat() - top.toFloat()) / 2f / c_stickRatio
                radius_StorokeInStick = (bottom.toFloat() - top.toFloat()) / 2f / c_stickInRatio
            }
            else {
                radius_Area = (right.toFloat() - left.toFloat()) / 2f
                radius_Stick = (right.toFloat() - left.toFloat()) / 2f / c_stickRatio
                radius_StorokeInStick = (right.toFloat() - left.toFloat()) / 2f / c_stickInRatio
            }
            maxShift = radius_Area - radius_Stick
        }

        if(move_X != 0f || move_Y != 0f) {
            //移動量が最大距離を上回る場合、角度を計算して最大距離に対するXとYを計算
            val touchShift: Double = Math.sqrt((move_X * move_X + move_Y * move_Y).toDouble())
            if(touchShift.toFloat() > maxShift) {
                val rad: Double = Math.atan((move_X / move_Y).toDouble())
                var x: Double = Math.abs(maxShift * Math.sin(rad))
                var y: Double = Math.abs(maxShift * Math.cos(rad))
                if(move_X < 0f) {
                    x = x * -1f
                }
                if(move_Y < 0f) {
                    y = y * -1f
                }
                nowCenter_X = center_X + x.toFloat()
                nowCenter_Y = center_Y + y.toFloat()
            }
            else {
                nowCenter_X = center_X + move_X
                nowCenter_Y = center_Y + move_Y
            }

            move_X = 0f
            move_Y = 0f
        }
        else {
            nowCenter_X = center_X
            nowCenter_Y = center_Y
        }

        MoveStickPercent_X = (nowCenter_X - center_X) / maxShift
        MoveStickPercent_Y = (nowCenter_Y - center_Y) / maxShift

        MoveTouchShift_X = move_X
        MoveTouchShift_Y = move_Y

        canvas?.drawRect(0f,0f, rect_width,rect_height, Paint_rect)
        canvas?.drawCircle(center_X, center_Y,radius_Area, Paint_Area)
        canvas?.drawCircle(nowCenter_X, nowCenter_Y, radius_Stick, Paint_Stick)
        canvas?.drawCircle(nowCenter_X, nowCenter_Y, radius_StorokeInStick, Paint_StickStroke)
        canvas?.drawCircle(nowCenter_X, nowCenter_Y, radius_StorokeInStick, Paint_StickStroke)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartPos_X = event.x
                touchStartPos_Y = event.y

                move_X = 0f
                move_Y = 0f
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                var nowPosX = event.x
                var nowPosY = event.y

                move_X = nowPosX - touchStartPos_X
                move_Y = nowPosY - touchStartPos_Y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchStartPos_X = 0f
                touchStartPos_Y = 0f

                move_X = 0f
                move_Y = 0f
                invalidate()
            }
            else -> {
            }
        }

        //Log.d("StickMovePercent","X:" + getMovePercent()[0].toString() + "    Y:" + getMovePercent()[1].toString())

        //return super.onTouchEvent(event)
        return true
    }

    public fun moveStick(X:Float, Y:Float){
        move_X = X
        move_Y = Y

        //再描画
        invalidate()
    }

    public fun getMovePercent(): FloatArray {
        val data = FloatArray(2)
        data[0] = (nowCenter_X - center_X) / maxShift
        data[1] = (nowCenter_Y - center_Y) / maxShift * -1
        return data
    }
}