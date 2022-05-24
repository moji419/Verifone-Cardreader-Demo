package com.gerege.cardreader_verifon.interfaces;

import com.gerege.cardreader_verifon.models.ICCard;
import com.gerege.cardreader_verifon.models.MagCard;

public interface CardReaderListener {
    /**
     * Карт унших процесс эхэлсэн
     */
    void onStart();

    /**
     * Magnetic карт амжилттай уншсан
     *
     * @param magCard Уншсан картны мэдээлэл
     * @param pin     ПИН
     */
    void onMagCard(MagCard magCard, String pin);

    /**
     * IC карт амжилттай уншсан
     *
     * @param icCard Уншсан картны мэдээлэл
     * @param pin    ПИН
     */
    void onIcCard(ICCard icCard, String pin);

    /**
     * Цуцлагдсан
     */
    void onCancelled();

    /**
     * Алдаа гарсан
     */
    void onError();
}
