package com.fiit.lusinda.translate;

import java.util.ArrayList;
import java.util.Random;

import com.fiit.lusinda.translate.GoogleTranslateStrategy;
import com.fiit.lusinda.translate.TranslateStrategy;
import com.fiit.lusinda.utils.Logging;
import com.memetix.mst.translate.Translate;

public class TextTranslator extends TranslateStrategy {

	private TranslateStrategy translate;

	public TextTranslator(TranslateStrategy strategy) {
		this.translate = strategy;
	}

	@Override
	public String translateText(String inputText, String source, String target) {

		String translated = "";

		ArrayList<String> chunks = (ArrayList<String>) getChunks(inputText,
				translate.numCharsPerRequest);


		for (String s : chunks) {
			try {
				if (source == null && target == null)
					translated += this.translate.translateText(s);
				else
					translated += this.translate.translateText(s,
							source, target);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return translated;

	}

	public String translateText(String inputText) throws Exception {

		return this.translateText(inputText, null, null);
	}

}
