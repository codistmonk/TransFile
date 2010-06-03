/*
 * Copyright © 2010 Martin Riedel
 * 
 * This file is part of TransFile.
 *
 * TransFile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TransFile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TransFile.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sourceforge.transfile.i18n;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

/**
 * Automated tests using JUnit 4 for {@link Translator}.
 *
 * @author codistmonk (creation 2010-06-03)
 */
public class TranslatorTest {

	/**
	 * Test method for {@link Translator#translate(Object, String, String, String, Object[])}.
	 */
	@Test
	public void testTranslateObject() {
		final String translationKey1 = "public_key";
		final String textPropertyName1 = "publicText";
		final String translationKey2 = "package_key";
		final String textPropertyName2 = "packageText";
		final Translator translator = new Translator();
		
		translator.setLocale(Locale.ENGLISH);
		
		final Translatable translatable = translator.translate(
				new Translatable(),
				textPropertyName1,
				translationKey1,
				MESSAGES_BASE);
		
		assertEquals("", translatable.getPackageText());
		
		translator.translate(translatable, textPropertyName2, translationKey2, MESSAGES_BASE);
		
		assertEquals("Public key", translatable.getPublicText());
		assertEquals("Package key", translatable.getPackageText());
		
		translator.setLocale(Locale.FRENCH);
		
		assertEquals("Clé publique", translatable.getPublicText());
		assertEquals("Clé package", translatable.getPackageText());
		
		translator.setLocale(Locale.ENGLISH);
		
		assertEquals("Public key", translatable.getPublicText());
		assertEquals("Package key", translatable.getPackageText());
	}
	
	/**
	 * Test method for {@link Translator#translate(Object, String, String, String, Object[])}.
	 */
	@Test
	public void testTranslateObjectWithParameterizedMessage() {
		final String translationKey = "life_universe_everything";
		final String textPropertyName = "parameterizedText";
		final Translator translator = new Translator();
		
		translator.setLocale(Locale.ENGLISH);
		
		final Translatable translatable = translator.translate(
				new Translatable(),
				textPropertyName,
				translationKey,
				MESSAGES_BASE,
				42);
		
		assertEquals("Answer: 42", translatable.getParameterizedText());
		
		translator.setLocale(Locale.FRENCH);
		
		assertEquals("Réponse : 42", translatable.getParameterizedText());
		
		translator.setLocale(Locale.ENGLISH);
		
		assertEquals("Answer: 42", translatable.getParameterizedText());
	}
	
	/**
	 * Test method for {@link Translator#untranslate(Object, String)}.
	 */
	@Test
	public void testUntranslateObject() {
		final String translationKey1 = "public_key";
		final String textPropertyName1 = "publicText";
		final String translationKey2 = "package_key";
		final String textPropertyName2 = "packageText";
		final Translator translator = new Translator();
		
		translator.setLocale(Locale.ENGLISH);
		
		final Translatable translatable = translator.translate(
				new Translatable(),
				textPropertyName1,
				translationKey1,
				MESSAGES_BASE);
		
		assertEquals("", translatable.getPackageText());
		
		translator.translate(translatable, textPropertyName2, translationKey2, MESSAGES_BASE);
		
		assertEquals("Public key", translatable.getPublicText());
		assertEquals("Package key", translatable.getPackageText());
		
		translator.setLocale(Locale.FRENCH);
		
		assertEquals("Clé publique", translatable.getPublicText());
		assertEquals("Clé package", translatable.getPackageText());
		
		translator.untranslate(translatable, textPropertyName1);
		translator.setLocale(Locale.ENGLISH);
		
		assertEquals(translationKey1, translatable.getPublicText());
		assertEquals("Package key", translatable.getPackageText());
		
	}

	/**
	 * Test method for {@link Translator#translate(String, String, Object[])}.
	 */
	@Test
	public void testTranslateMessage() {
		final String translationKey = "life_universe_everything";
		final Translator translator = new Translator();
		
		{
			translator.setLocale(Locale.ENGLISH);
			
			final String result = translator.translate(translationKey, MESSAGES_BASE, 42);
			
			assertNotNull(result);
			assertEquals("Answer: 42", result);
		}
		{
			translator.setLocale(Locale.FRENCH);
			
			final String result = translator.translate(translationKey, MESSAGES_BASE, 42);
			
			assertNotNull(result);
			assertEquals("Réponse : 42", result);
		}
	}
	
	/**
	 * Test method for {@link Translator#createLocale(String)}.
	 */
	@Test
	public final void testCreateLocale() {
		{
			final Locale result = Translator.createLocale("fr");
			
			assertNotNull(result);
			assertEquals(Locale.FRENCH, result);
		}
		{
			final Locale result = Translator.createLocale("fr_CA");
			
			assertNotNull(result);
			assertEquals(Locale.CANADA_FRENCH, result);
		}
		{
			final Locale result = Translator.createLocale("fr_FR_parisien");
			
			assertNotNull(result);
			assertEquals(new Locale("fr", "FR", "parisien"), result);
		}
	}
	
	/**
	 * Test method for {@link Translator#getLanguageCountryVariant(Locale)}.
	 */
	@Test
	public final void testGetLanguageCountryVariant() {
		final String language = "fr";
		final String country = "FR";
		final String variant = "parisien";
		
		{
			final String result = Translator.getLanguageCountryVariant(new Locale(language));
			
			assertNotNull(result);
			assertEquals(language, result);
			
		}
		{
			final String result = Translator.getLanguageCountryVariant(new Locale(language, country));
			
			assertNotNull(result);
			assertEquals(language + "_" + country, result);
			
		}
		{
			final String result = Translator.getLanguageCountryVariant(new Locale(language, country, variant));
			
			assertNotNull(result);
			assertEquals(language + "_" + country + "_" + variant, result);
			
		}
	}
	
	private static final String MESSAGES_BASE = TranslatorTest.class.getPackage().getName().replaceAll("\\.", "/") + "/messages";
	
	/**
	 * 
	 * @author codistmonk (creation 2010-06-03)
	 */
	private static class Translatable {
		
		private String publicText;
		
		private String parameterizedText;
		
		private String packageText;
		
		Translatable() {
			this.publicText = "";
			this.packageText = "";
			this.parameterizedText = "";
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 */
		public final String getPublicText() {
			return this.publicText;
		}
		
		/**
		 * 
		 * @param publicText
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		@SuppressWarnings("unused")
		public final void setPublicText(final String publicText) {
			this.publicText = publicText;
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 */
		public final String getParameterizedText() {
			return this.parameterizedText;
		}
		
		/**
		 * 
		 * @param parameterizedText
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		@SuppressWarnings("unused")
		public final void setParameterizedText(final String parameterizedText) {
			this.parameterizedText = parameterizedText;
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 */
		final String getPackageText() {
			return this.packageText;
		}
		
		/**
		 * 
		 * @param packageText
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		@SuppressWarnings("unused")
		final void setPackageText(final String packageText) {
			this.packageText = packageText;
		}
		
	}
	
}
