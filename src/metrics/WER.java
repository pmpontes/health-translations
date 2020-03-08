package metrics;

import utils.Log;

import java.util.Arrays;

public class WER {

    public static double calculate(String hyp, String ref) {
        String[] r = ref.split(" ");
        String[] h = hyp.split(" ");

        double[][] d = new double[(h.length + 1)][(r.length + 1)];

        for (int i = 0; i < h.length + 1; i++) {
            for (int j = 0; j < r.length + 1; j++) {
                if (i == 0) {
                    d[0][j] = j;
                } else if (j == 0) {
                    d[i][0] = i;
                }
            }
        }

        for (int i = 1; i < h.length + 1; i++) {
            for (int j = 1; j < r.length + 1; j++) {
                if (r[j-1].equals(h[i-1])) {
                    d[i][j] = d[i - 1][j - 1];
                } else if (Arrays.asList(r).contains(h[i-1])) {
                    double substitution = d[i-1][j-1] + .25;
                    double insertion = d[i][j-1] + 1;
                    double deletion = d[i-1][j] + 1;
                    Log.error("here");
                    d[i][j] = Math.min(substitution, Math.min(insertion, deletion));
                } else {
                    double substitution = d[i-1][j-1] + 1;
                    double insertion = d[i][j-1] + 1;
                    double deletion = d[i-1][j] + 1;
                    d[i][j] = Math.min(substitution, Math.min(insertion, deletion));
                }
            }
        }

        if (r.length == 0) {
            return h.length != 0 ? 1 : 0;
        }

        return d[h.length][r.length] / (float) r.length;
    }

    public static void main(String[] args) {
        Log.info(calculate("coração ataque", "ataque de coração") +"");
    }
}
