package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final int N = 4 ;
    private final int[][] cells = new int[N][N] ;  // значения в ячейках поля
    private final int[][] saves = new int[N][N] ;  // предыдущий ход
    private final TextView[][] tvCells = new TextView[N][N] ;   // ссылки на ячеки поля
    private final Random random = new Random() ;
    private final String BEST_SCORE_FILENAME = "best_score.txt" ;

    private int score ;
    private int bestScore ;
    private int saveScore ;
    private boolean continuePlaying ;
    private TextView tvScore ;
    private TextView tvBestScore ;
    private Animation spawnAnimation ;
    private Animation collapseAnimation ;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvScore = findViewById( R.id.game_tv_score ) ;
        tvBestScore = findViewById( R.id.game_tv_best_score ) ;
        tvScore.setText( getString( R.string.game_score, "69.6k" ) ) ;
        tvBestScore.setText( getString( R.string.game_best_score, "69.6k" ) ) ;

        spawnAnimation = AnimationUtils.loadAnimation( this, R.anim.cell_spawn ) ;
        spawnAnimation.reset() ;
        collapseAnimation = AnimationUtils.loadAnimation( this, R.anim.cell_collapse ) ;
        collapseAnimation.reset() ;

        for( int i = 0; i < N; ++i ) {
            for( int j = 0; j < N; ++j ) {
                tvCells[i][j] = findViewById(     // R.id.game_cell_12
                        getResources().getIdentifier(
                                "game_cell_" + i + j,
                                "id",
                                getPackageName()
                        )
                ) ;
            }
        }

        findViewById( R.id.game_field )
                .setOnTouchListener(
                        new OnSwipeTouchListener( GameActivity.this ) {
                            @Override
                            public void onSwipeRight() {
                                if( canMoveRight() ) {
                                    saveField() ;
                                    moveRight() ;
                                    spawnCell() ;
                                    showField() ;
                                }
                                else {
                                    Toast.makeText(
                                                    GameActivity.this,
                                                    "No Right Move",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                            @Override
                            public void onSwipeLeft() {
                                if( canMoveLeft() ) {
                                    saveField() ;
                                    moveLeft() ;
                                    spawnCell() ;
                                    showField() ;
                                }
                                else {
                                    Toast.makeText(
                                                    GameActivity.this,
                                                    "No Left Move",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        }
                ) ;
        findViewById( R.id.game_new ).setOnClickListener( this::newGame ) ;
        findViewById( R.id.game_undo ).setOnClickListener( this::undoMoveClick ) ;
        newGame( null ) ;
    }
    private void newGame( View view ) {
        for( int i = 0; i < N; ++i ) {
            for( int j = 0; j < N; ++j ) {
                cells[i][j] = 0 ;
            }
        }
        continuePlaying = false ;
        score = 0 ;
        loadBestScore() ;
        tvBestScore.setText( getString( R.string.game_best_score, String.valueOf( bestScore ) ) ) ;
        spawnCell() ;
        spawnCell() ;
        saveField() ;
        showField() ;
    }
    private boolean canMoveRight() {
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N-1; j++ ) {
                if( cells[ i ][ j ] != 0 && cells[ i ][ j + 1 ] == 0
                 || cells[ i ][ j ] != 0 && cells[ i ][ j ] == cells[ i ][ j + 1 ] ) {
                    return true ;
                }
            }
        }
        /*
        Д.З. Реализовать проверку возможности хода вправо (без изменения состояния поля)
        ** реализовать ходы и проверки по другим направлениям
         */
        return false ;
    }
    private boolean canMoveLeft() {
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N-1; j++ ) {
                if( cells[ i ][ j ] == 0 && cells[ i ][ j + 1 ] != 0
                        || cells[ i ][ j ] != 0 && cells[ i ][ j ] == cells[ i ][ j + 1 ] ) {
                    return true ;
                }
            }
        }
        return false ;
    }
    @SuppressLint("DiscouragedApi")
    private void showField() {
        Resources resources = getResources() ;
        String packageName = getPackageName() ;
        for( int i = 0; i < N; ++i ) {
            for( int j = 0; j < N; ++j ) {
                tvCells[i][j].setText( String.valueOf( cells[i][j] ) ) ;
                tvCells[i][j].setTextAppearance(    // R.style.GameCell_16
                        resources.getIdentifier(
                                "GameCell_" + cells[i][j],
                                "style",
                                packageName
                        )
                ) ;
                // setTextAppearance не "подтягивает" фоновый цвет
                tvCells[i][j].setBackgroundColor(
                        resources.getColor(     // R.color.game_bg_16,
                                resources.getIdentifier(
                                        "game_bg_" + cells[i][j],
                                        "color",
                                        packageName
                                ),
                                getTheme()
                        )
                ) ;
            }
        }
        tvScore.setText( getString( R.string.game_score, String.valueOf( score ) ) ) ;
        if( score > bestScore ) {
            bestScore = score ;
            saveBestScore() ;
            tvBestScore.setText( getString( R.string.game_best_score, String.valueOf( bestScore ) ) ) ;
        }
        if( score >= 8 && ! continuePlaying ) {  // !!
            showWinMessage() ;
        }
    }
    private boolean spawnCell() {
        // собираем данные о пустых ячейках
        List<Coord> coordinates = new ArrayList<>() ;
        for( int i = 0; i < N; ++i ) {
            for( int j = 0; j < N; ++j ) {
                if( cells[i][j] == 0 ) {
                    coordinates.add( new Coord( i, j ) ) ;
                }
            }
        }
        // проверяем есть ли пустые ячейки
        int cnt = coordinates.size() ;
        if( cnt == 0 ) return false ;
        // генерируем случайный индекс
        int randIndex = random.nextInt( cnt ) ;
        // извлекаем координаты
        int x = coordinates.get( randIndex ).getX() ;
        int y = coordinates.get( randIndex ).getY() ;
        // ставим в ячейку 2 / 4
        cells[x][y] = random.nextInt( 10 ) == 0 ? 4 : 2 ;
        // проигрываем анимацию для появившейся ячейки
        tvCells[x][y].startAnimation( spawnAnimation ) ;
        return true ;
    }
    private void moveRight() {
        for( int i = 0; i < N; ++i ) {
            // сдвиги
            boolean wasReplace;
            do {
                wasReplace = false;
                for (int j = N - 1; j > 0; --j) {
                    if (cells[i][j] == 0          // текущая ячейка 0
                            && cells[i][j - 1] != 0) {    // а перед ней - не 0
                        cells[i][j] = cells[i][j - 1];    // [2    4     2     0]
                        cells[i][j - 1] = 0;              //             j-1   j
                        wasReplace = true;                // [2    4     0     2]
                    }                                     //       j-1   j
                }
            } while (wasReplace);

            // collapse
            for (int j = N - 1; j > 0; --j) {  // [2202] -> [0222] -> [0204] -> [0024]
                if (cells[i][j] == cells[i][j - 1] && cells[i][j] != 0) {  // соседние ячейки равны  [2222]
                    score += cells[i][j] + cells[i][j - 1] ;   // счет = сумма всех объединенных ячеек
                    cells[i][j] *= -2 ;  // [2224]; "-" - признак для анимации
                    cells[i][j - 1] = 0 ;   // [2204]
                }
            }  // [0404]  при коллапсе может понадобиться дополнительное смещение
            for (int j = N - 1; j > 0; --j) {
                if (cells[i][j] == 0 && cells[i][j - 1] != 0) {
                    cells[i][j] = cells[i][j - 1];
                    cells[i][j - 1] = 0;
                }
            }
            for (int j = N - 1; j > 0; --j) {
                if( cells[i][j] < 0 ) {  // надо включить анимацию
                    cells[i][j] = -cells[i][j] ;
                    tvCells[i][j].startAnimation( collapseAnimation ) ;
                }
            } // [0044]
        }
    }

    private void moveLeft() {
        for( int i = 0; i < N; i++ ) {   // loop rows
            int k = -1 ;  // stack head
            // collapse
            for( int j = 0; j < N; j++ ) {
                if( cells[ i ][ j ] != 0 ) {
                    if( k == -1 || cells[ i ][ j ] != cells[ i ][ k ] ) {
                        k = j ;
                    }
                    else {
                        cells[ i ][ k ] += cells[ i ][ j ] ;
                        score += cells[ i ][ k ] ;
                        cells[ i ][ j ] = 0 ;
                        k = -1;
                    }
                }
            }
            // move
            k = 0;
            for( int j = 0; j < N; j++ ) {
                if( cells[ i ][ j ] != 0 ) {
                    cells[ i ][ k ] = cells[ i ][ j ] ;
                    if( j != k ) cells[ i ][ j ] = 0 ;
                    ++k;
                }
            }
        }
    }
    private void saveField() {
        for (int i = 0; i < N; i++) {
            System.arraycopy(cells[i], 0, saves[i], 0, N);
        }
        saveScore = score ;
    }
    private void undoMove() {
        for (int i = 0; i < N; i++) {
            System.arraycopy(saves[i], 0, cells[i], 0, N);
        }
        score = saveScore ;
    }
    private void undoMoveClick( View view ) {
        undoMove() ;
        showField() ;
    }
    /*
    Д.З. Провести рефакторинг spawnCell()
    добавить параметр с кол-вом появляющихся ячеек.
    * Реализовать один из ходов (в любую сторону)
     */

    private void saveBestScore() {
        try(  FileOutputStream fileStream = openFileOutput( BEST_SCORE_FILENAME, Context.MODE_PRIVATE ) ;
              DataOutputStream writer = new DataOutputStream( fileStream ) ) {
            writer.writeInt( bestScore ) ;
            writer.flush() ;
        }
        catch( IOException ex ) {
            Log.d( "saveBestScore", ex.getMessage() ) ;
        }
    }
    private void loadBestScore() {
        try( FileInputStream fileInputStream = openFileInput( BEST_SCORE_FILENAME );
             DataInputStream reader = new DataInputStream( fileInputStream ) ) {
            bestScore = reader.readInt() ;
        }
        catch( IOException ex ) {
            Log.d( "loadBestScore", ex.getMessage() ) ;
            bestScore = 0 ;
        }
    }
    private void showWinMessage() {
        new AlertDialog.Builder( this, com.google.android.material.R.style.Base_Theme_Material3_Dark_Dialog ) // com.google.android.material.R.style.Base_Theme_Material3_Dark )
                .setTitle( R.string.game_win_dialog_title )
                .setMessage( R.string.game_win_dialog_message )
                .setIcon( android.R.drawable.btn_star )
                .setCancelable( false )
                .setPositiveButton( R.string.game_yes_dialog_button, ( dialog, button ) -> continuePlaying = true )
                .setNegativeButton( R.string.game_exit_dialog_button,  ( dialog, button ) -> finish() )
                .setNeutralButton( R.string.game_new_dialog_button, ( dialog, button ) -> newGame( null ) )
                .show() ;
    }

    private static class Coord {
        private final int x ;
        private final int y ;

        public Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
/*
Анимации
1. Создаем ресурсную папку res/anim (ПКМ - папка - anim)
2. Создаем ресурсный файл cell_spawn.xml, корневой элемент alpha (см файл)
3. Создаем переменную типа Animation spawnAnimation
4. Загружаем анимацию
    spawnAnimation = AnimationUtils.loadAnimation( this, R.anim.cell_spawn ) ;
    spawnAnimation.reset() ;
5. Проигрываем анимацию (на любом View)
    tvCells[x][y].startAnimation( spawnAnimation ) ;

Виды анимаций
alpha - прозрачность
rotate - вращение
translate - перемещение
scale - размер(масштаб)
 */