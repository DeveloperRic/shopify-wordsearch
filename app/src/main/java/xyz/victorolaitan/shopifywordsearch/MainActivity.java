package xyz.victorolaitan.shopifywordsearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

@SuppressWarnings("SpellCheckingInspection")
@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String[] REQUIRED_WORDS = new String[]
            {"SWIFT", "KOTLIN", "OBJECTIVEC", "VARIABLE", "JAVA", "MOBILE"};

    private Character[][] grid;
    private String[] words;
    private int current = -1;
    private int foundCount = 0;

    private TextView txtNextWord;
    private TextView txtFoundCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtNextWord = findViewById(R.id.txtNextWord);
        txtFoundCount = findViewById(R.id.txtFoundCount);

        init();
    }

    private void init() {
        GridView gridView = findViewById(R.id.gridview);
        gridView.setAdapter(new GridAdapter(this, generateGrid()));
        current = 0;
        foundCount = 0;
        updateViews();
        Toast.makeText(this, "Rotating your phone resets the gridview", Toast.LENGTH_LONG).show();
    }

    private char[] generateGrid() {
        grid = new Character[10][10];
        words = new String[REQUIRED_WORDS.length];
        System.arraycopy(REQUIRED_WORDS, 0, words, 0, REQUIRED_WORDS.length);
        Random rand = new Random();
        int[] rowOrder = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            rowOrder[i] = rand.nextInt(10);
            for (int j = 0; j < words.length; j++) {
                if (j == i) continue;
                if (rowOrder[j] == rowOrder[i]) {
                    i--;
                    break;
                }
            }
        }
        System.out.println("\n=============\n");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int row = rowOrder[i];
            int offset = rand.nextInt(10 - word.length() + 1);
            System.out.println(word + " [" + row + "," + offset + "]");
            for (int j = 0; j < word.length(); j++) {
                grid[row][j + offset] = word.charAt(j);
            }
        }
        System.out.println("\n=============\n");
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (grid[i][j] == null) {
                    grid[i][j] = ALPHABET.charAt(rand.nextInt(ALPHABET.length()));
                }
            }
        }
        System.out.println("\n=============\n");
        char[] result = new char[100];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) result[(10 * i) + j] = grid[i][j];
        }
        return result;
    }

    private int searchForAnswer(int position) {
        int row = position / 10;
        int column = position % 10;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word == null) continue;
            if (column + word.length() > 10) continue;
            boolean found = true;
            for (int j = 0; j < word.length(); j++) {
                if (grid[row][column + j] != word.charAt(j)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                foundCount++;
                words[i] = null;
                current = -1;
                for (int j = 0; j < words.length; j++) {
                    if (words[j] != null) {
                        current = j;
                        break;
                    }
                }
                if (current < 0) {
                    new AlertDialog.Builder(this)
                            .setMessage("You've completed the puzzle, congrats!")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    init();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                } else {
                    Toast.makeText(this, "You found " + word + "!", Toast.LENGTH_LONG).show();
                    updateViews();
                }
                return word.length();
            }
        }
        return 0;
    }

    private void updateViews() {
        if (current >= 0) {
            txtNextWord.setText(words[current]);
            txtFoundCount.setText(foundCount + "/" + words.length);
        }
    }

    public class GridAdapter extends BaseAdapter {
        private final Context mContext;
        private char[] letters;
        private TextView[] textViews;

        GridAdapter(Context context, char[] letters) {
            this.mContext = context;
            this.letters = letters;
            this.textViews = new TextView[letters.length];
        }

        @Override
        public int getCount() {
            return letters.length;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return letters[position];
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View gridView;
            if (convertView == null) {
                gridView = LayoutInflater.from(mContext)
                        .inflate(R.layout.gird_letter, parent, false);
                final TextView txtLetter = gridView.findViewById(R.id.gridview_txtLetter);
                textViews[position] = txtLetter;
                txtLetter.setText("" + letters[position]);
                txtLetter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int length = searchForAnswer(position);
                        if (length > 0) {
                            for (int i = 0; i < length; i++) {
                                textViews[position + i].setTextColor(getResources().getColor(R.color.colorAccent));
                            }
                        }
                    }
                });
            } else {
                gridView = convertView;
            }
            return gridView;
        }

    }
}
