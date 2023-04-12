package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final int N = 4 ;
    private final int[][] cells = new int[N][N] ;  // значения в ячейках поля
    private final TextView[][] tvCells = new TextView[N][N] ;   // ссылки на ячеки поля
    private final Random random = new Random() ;

    private int score ;
    private int bestScore ;
    private TextView tvScore ;
    private TextView tvBestScore ;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvScore = findViewById( R.id.game_tv_score ) ;
        tvBestScore = findViewById( R.id.game_tv_best_score ) ;
        tvScore.setText( getString( R.string.game_score, "69.6k" ) ) ;
        tvBestScore.setText( getString( R.string.game_best_score, "69.6k" ) ) ;

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
                                Toast.makeText(
                                        GameActivity.this,
                                        "Right",
                                        Toast.LENGTH_SHORT)
                                    .show();
                            }
                            @Override
                            public void onSwipeLeft() {
                                Toast.makeText(
                                                GameActivity.this,
                                                "Left",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                ) ;

        newGame() ;
    }
    private void newGame() {
        for( int i = 0; i < N; ++i ) {
            for( int j = 0; j < N; ++j ) {
                cells[i][j] = 0 ;
            }
        }
        score = 0 ;
        spawnCell() ;
        spawnCell() ;
        showField() ;
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

        return true ;
    }
    /*
    Д.З. Провести рефакторинг spawnCell()
    добавить параметр с кол-вом появляющихся ячеек.
    * Реализовать один из ходов (в любую сторону)
     */

    private class Coord {
        private int x ;
        private int y ;

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
Д.З. Реализовать все свайпы
Подобрать цвета для ячеек с различными значениями
 */