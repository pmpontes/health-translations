package metrics;

import metrics.tercom.TERcalc;
import metrics.tercom.TERcost;
import utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ter {
    public static void main(String[] args) {
        TERcost costs = new TERcost();
        costs._shift_cost = .25;
        costs._insert_cost = .25;

        Log.info(TERcalc.TER("coração ataque", "ataque cardíaco", costs).score() + "");
        Log.info(TERcalc.TER("ataque coração", "ataque de coração", costs).score() + "");

    }
}
