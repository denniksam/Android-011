package step.learning.course;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class CalcActivity extends AppCompatActivity {

    private TextView tvHistory ;
    private TextView tvResult ;
    private String minusSign ;
    private String zeroSymbol ;
    private boolean needClear ;  // необходимо очистить экран при вводе новой цифры
    /*
        Д.З. Реализовать сохранение и восстановление (при изменении конфигурации)
        всех необходимых значений, влияющих на состояние калькулятора
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        minusSign = getString( R.string.calc_minus_sign ) ;
        zeroSymbol = getString( R.string.calc_btn_0_text ) ;

        tvHistory = findViewById( R.id.tv_history ) ;
        tvResult = findViewById( R.id.tv_result ) ;

        clearClick( null ) ;

        // String[] suffixes = {"one", "two", } ;
        for( int i = 0; i < 10; i++ ) {
            @SuppressLint("DiscouragedApi")   // более эффективно - R.id.calc_btn_5
            int buttonId = getResources().getIdentifier(
                    "calc_btn_" + i,    // suffixes[i],
                    "id",
                    getPackageName()
            ) ;
            findViewById( buttonId ).setOnClickListener( this::digitClick ) ;
        }
        findViewById( R.id.calc_btn_backspace ).setOnClickListener( this::backspaceClick ) ;
        findViewById( R.id.calc_btn_plus_minus ).setOnClickListener( this::plusMinusClick ) ;
        findViewById( R.id.calc_btn_clear ).setOnClickListener( this::clearClick ) ;
        findViewById( R.id.calc_btn_ce ).setOnClickListener( this::clearEditClick ) ;
        findViewById( R.id.calc_btn_square ).setOnClickListener( this::squareClick ) ;
        findViewById( R.id.calc_btn_sqrt ).setOnClickListener( this::sqrtClick ) ;

    }
    private void sqrtClick( View view ) {
        String result = tvResult.getText().toString() ;
        double arg ;
        try {
            arg = Double.parseDouble(
                    result
                            .replace( minusSign, "-" )
                            .replaceAll( zeroSymbol, "0" )
            ) ;
        }
        catch( NumberFormatException | NullPointerException ignored ) {
            Toast.makeText(                     // Всплывающее сообщение
                            this,                       // контекст - родительская активность
                            R.string.calc_error_parse,  // текст либо ресурс
                            Toast.LENGTH_SHORT )        // длительность (во времени)
                    .show();                        // !! не забывать - запуск тоста
            return ;
        }
        if( arg < 0 ) {
            // Корень из отрицательного числа не извлекается (в действительных числах)
            /* Доступ к системным устройствам на примере вибрации
            Прежде всего, нужно получить разрешение на использование устройства.
            Некоторые устройства не требуют подтверждение от пользователя, но все они
            должны запросить разрешение от системы.
            Заявка на доступ к устройствам (и другие разрешения) указываются в манифесте
                <uses-permission android:name="android.permission.VIBRATE"/>
            Дальнейшая работа с устройством может зависеть от версии API на которую рассчитано
            приложение.
             */
            /* Самый простой подход - deprecated from O (Oreo, API 26)
            Vibrator vibrator = (Vibrator) getSystemService( Context.VIBRATOR_SERVICE ) ;
            vibrator.vibrate( 250 ) ;   // вибрация 250 мс
            */
            // начиная с S (API 31) изменились правила доступа к устройствам
            Vibrator vibrator ;
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ) {
                VibratorManager vibratorManager = (VibratorManager)
                        getSystemService( Context.VIBRATOR_MANAGER_SERVICE ) ;
                vibrator = vibratorManager.getDefaultVibrator() ;
            }
            else {
                vibrator = (Vibrator) getSystemService( Context.VIBRATOR_SERVICE ) ;
            }

            // шаблон вибрации 1 - пауза, 2 - работа, 3 - пауза, 4 - работа, .....
            long[] vibratePattern = { 0, 200, 100, 200 } ;

            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
                // однократное включение
                // vibrator.vibrate( VibrationEffect.createOneShot( 250, VibrationEffect.DEFAULT_AMPLITUDE ) ) ;

                vibrator.vibrate(
                        VibrationEffect.createWaveform(
                                vibratePattern, -1   // индекс повтора, -1 - без повторов, один раз
                        )
                ) ;
            }
            else {
                // vibrator.vibrate( 250 ) ;   // вибрация 250 мс - однократно
                vibrator.vibrate( vibratePattern, -1 ) ;  // по шаблону
            }
/*
            Получение вибратора:
                        vibratorManager
            API >= 31 <
                        getSystemService

            Использование:
                        vibrator.vibrate( VibrationEffect.... )
            API >= 26 <
                        vibrator.vibrate()

v.vibrate(new long[]{0, 500, 110, 500, 110, 450, 110, 200, 110, 170, 40, 450, 110, 200, 110, 170, 40, 500}, -1);
 */
        }
    }
    // При изменении конфигурации устройства перезапускается активность и данные исчезают

    // Данный метод-событие вызывается при разрушении данной конфигурации
    @Override
    protected void onSaveInstanceState( @NonNull Bundle savingState ) {
        // savingState - ~словарь сохраняющихся данных
        super.onSaveInstanceState( savingState ) ;  // Оставить, нужно обязательно
        Log.d( "CalcActivity", "onSaveInstanceState" ) ;
        // добавляем к сохраняемым данным свои значения
        savingState.putCharSequence( "history", tvHistory.getText() ) ;
        savingState.putCharSequence( "result", tvResult.getText() ) ;
    }
    // Вызов при восстановлении конфигурации
    @Override
    protected void onRestoreInstanceState( @NonNull Bundle savedState ) {
        super.onRestoreInstanceState( savedState ) ;
        Log.d( "CalcActivity", "onRestoreInstanceState" ) ;

        tvHistory.setText( savedState.getCharSequence( "history" ) ) ;
        tvResult.setText( savedState.getCharSequence( "result" ) ) ;
    }

    private void squareClick( View view ) {
        String result = tvResult.getText().toString() ;
        double arg ;
        try {
             arg = Double.parseDouble(
                    result
                            .replace( minusSign, "-" )
                            .replaceAll( zeroSymbol, "0" )
             ) ;
        }
        catch( NumberFormatException | NullPointerException ignored ) {
            Toast.makeText(                     // Всплывающее сообщение
                    this,                       // контекст - родительская активность
                    R.string.calc_error_parse,  // текст либо ресурс
                    Toast.LENGTH_SHORT )        // длительность (во времени)
                .show();                        // !! не забывать - запуск тоста
            return ;
        }
        tvHistory.setText( getString( R.string.calc_square_history, result ) ) ;
        arg *= arg ;
        displayResult( arg ) ;
        needClear = true ;
        /*
        Д.З. После вычисления результата операции "квадрат" при нажатии "backspace"
        должен полностью очищаться экран и история.
        При начале ввода (после операции) также должна стираться история.
        Реализовать операцию 1/x (инверсию)
        */
    }
    private void clearClick( View view ) {  // C
        tvHistory.setText( "" ) ;
        displayResult( "" ) ;
    }
    private void clearEditClick( View view ) {  // CE
        displayResult( "" ) ;
    }
    private void plusMinusClick( View view ) {
        // изменение знака: если есть "-" перед числом, то убираем его, если нет - добавляем
        String result = tvResult.getText().toString() ;
        if( result.equals( zeroSymbol ) ) {
            return ;   // перед "0" знак не ставить
        }
        if( result.startsWith( minusSign ) ) {
            result = result.substring( 1 ) ;
        }
        else {
            result = minusSign + result ;
        }
        displayResult( result ) ;
    }
    private void backspaceClick( View view ) {
        String result = tvResult.getText().toString() ;
        result = result.substring( 0, result.length() - 1 ) ;
        displayResult( result ) ;
    }
    private void digitClick( View view ) {
        String result = tvResult.getText().toString() ;
        if( result.length() >= 10 ) {
            return ;
        }
        String digit = ( (Button) view ).getText().toString() ;
        if( result.equals( zeroSymbol ) || needClear ) {
            result = "" ;
            needClear = false ;
        }
        result += digit ;
        displayResult( result ) ;
// Ø
    }
    private void displayResult( String result ) {
        if( "".equals( result ) || minusSign.equals( result ) ) {
            result = zeroSymbol ;
        }
        tvResult.setText( result ) ;
    }
    private void displayResult( double arg ) {
        long argInt = (long) arg ;
        String result = argInt == arg ? "" + argInt : "" + arg ;

        result = result
                .replace( "-", minusSign )
                .replaceAll( "0", zeroSymbol ) ;

        displayResult( result ) ;
    }
}
/*
Д.З. Реализовать работу кнопки "," (десятичная запятая)
- символ запятой задается ресурсами (подобрать симпатичный символ)
- запятая в числе только одна
- в общий лимит цифр числа (10 цифр) символы "-" и "," не включается

 */