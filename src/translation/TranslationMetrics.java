package translation;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import metrics.jbleu.JBLEU;
import metrics.tercom.TERcalc;
import metrics.tercom.TERcost;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TranslationMetrics implements Serializable {
    private double bleuScore = 0;
    private double terScore = 1;
    private TERcost terCostFunction;

    public TranslationMetrics(List<Translation> translationsGenerated, List<String> correctTranslations) {
        terCostFunction = new TERcost();
        terCostFunction._shift_cost = .25;
        terCostFunction._insert_cost = .25;

        translationsGenerated.forEach(translation -> {
            JBLEU bleu = new JBLEU();
            int[] result = new int[JBLEU.getSuffStatCount()];

            // set BLEU hypothesis
            List<String> hyp = Lists.newArrayList(Splitter.on(' ').split(translation.getTranslationGenerated()));
            List<List<String>> refs = new ArrayList<>();

            correctTranslations.forEach(correctTranslation -> {
                // calculate and update TER score
                double terScore = TERcalc.TER(translation.getTranslationGenerated(), correctTranslation, terCostFunction).score();
                if (terScore < this.terScore) {
                    this.terScore = terScore;
                }

                // add reference to BLEU model
                refs.add(Lists.newArrayList(Splitter.on(' ').split(correctTranslation)));
            });

            // calculate and update BLEU score
            bleu.stats(hyp, refs, result);
            double bleuScore = bleu.score(result);
            if (bleuScore > this.bleuScore) {
                this.bleuScore = bleuScore;
            }
        });
    }

    public double getBleuScore() {
        return bleuScore;
    }

    public double getTerScore() {
        return terScore;
    }
}
