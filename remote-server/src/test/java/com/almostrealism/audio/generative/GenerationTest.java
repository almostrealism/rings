package com.almostrealism.audio.generative;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.almostrealism.audio.notes.PatternNoteSource;
import org.almostrealism.audio.pattern.PatternElementFactory;
import org.almostrealism.audio.pattern.PatternFactoryChoice;
import org.almostrealism.audio.pattern.PatternFactoryChoiceList;
import org.almostrealism.audioml.DiffusionGenerationProvider;
import org.almostrealism.audioml.LocalResourceManager;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GenerationTest {
	public static final String ROOT = "/Users/michael/AlmostRealism/";

	@Test
	public void generate() throws IOException {
		List<PatternFactoryChoice> choices =
				new ObjectMapper().readValue(new File(ROOT + "ringsdesktop/pattern-factory.json"),
					PatternFactoryChoiceList.class);

		List<PatternNoteSource> sources = choices.stream()
				.map(PatternFactoryChoice::getFactory)
				.filter(c -> "Hats".equals(c.getName()))
				.map(PatternElementFactory::getSources)
				.findFirst().orElseThrow();

		DiffusionGenerationProvider provider =
				new DiffusionGenerationProvider(new LocalResourceManager(
						new File(ROOT + "remote-models")));
		provider.refresh("test", sources);
		provider.generate("test", 10);
	}
}
