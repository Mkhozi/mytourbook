/*******************************************************************************
 * Copyright (c) 2007 Pascal Essiembre.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pascal Essiembre - initial API and implementation
 ******************************************************************************/
package org.eclipse.babel.core.message.resource.ser;

import java.util.Arrays;
import java.util.Properties;

import org.eclipse.babel.core.message.Message;
import org.eclipse.babel.core.message.MessagesBundle;

/**
 * Class responsible for serializing a {@link MessagesBundle} into {@link Properties}-like text.
 * 
 * @author Pascal Essiembre (pascal@essiembre.com)
 */
public class PropertiesSerializer {

	/** Generator header comment. */
	public static final String			GENERATED_BY				= "#Generated by Eclipse Messages Editor " //$NON-NLS-1$
																			+ "(Eclipse Babel)";			//$NON-NLS-1$

	/** A table of hex digits */
	private static final char[]			HEX_DIGITS					= {
			'0',
			'1',
			'2',
			'3',
			'4',
			'5',
			'6',
			'7',
			'8',
			'9',
			'A',
			'B',
			'C',
			'D',
			'E',
			'F'													};

	/** Special resource bundle characters when persisting any text. */
	private static final String			SPECIAL_VALUE_SAVE_CHARS	= "\t\f";								//$NON-NLS-1$

	/** Special resource bundle characters when persisting keys. */
	private static final String			SPECIAL_KEY_SAVE_CHARS		= "=\t\f#!: ";							//$NON-NLS-1$

	/** System line separator. */
	private static final String			SYSTEM_LINE_SEP				= System.getProperty("line.separator"); //$NON-NLS-1$

	/** Forced line separators. */
	private static final String[]		FORCED_LINE_SEP				= new String[4];
	static {
		FORCED_LINE_SEP[IPropertiesSerializerConfig.NEW_LINE_DEFAULT] = null;
		FORCED_LINE_SEP[IPropertiesSerializerConfig.NEW_LINE_UNIX] = "\\\\n"; //$NON-NLS-1$
		FORCED_LINE_SEP[IPropertiesSerializerConfig.NEW_LINE_WIN] = "\\\\r\\\\n"; //$NON-NLS-1$
		FORCED_LINE_SEP[IPropertiesSerializerConfig.NEW_LINE_MAC] = "\\\\r"; //$NON-NLS-1$
	}

	private IPropertiesSerializerConfig	config;

	/**
	 * Constructor.
	 */
	public PropertiesSerializer(final IPropertiesSerializerConfig config) {
		super();
		this.config = config;
	}

	/**
	 * Appends a key to resource bundle content.
	 * 
	 * @param text
	 *            the resource bundle content so far
	 * @param key
	 *            the key to add
	 * @param equalIndex
	 *            the equal sign position
	 * @param active
	 *            is the key active or not
	 */
	private void appendKey(final StringBuffer text, final String key, final int equalIndex, final boolean active) {

		if (!active) {
			text.append("##"); //$NON-NLS-1$
		}

		// Escape and persist the rest
		saveKey(text, key);
//        text.append(key);
		for (int i = 0; i < equalIndex - key.length(); i++) {
			text.append(' ');
		}
		if (config.isSpacesAroundEqualsEnabled()) {
			text.append(" = "); //$NON-NLS-1$
		} else {
			text.append("="); //$NON-NLS-1$
		}
	}

	/**
	 * Appends a value to resource bundle content.
	 * 
	 * @param text
	 *            the resource bundle content so far
	 * @param value
	 *            the value to add
	 * @param equalIndex
	 *            the equal sign position
	 * @param active
	 *            is the value active or not
	 */
	private void appendValue(final StringBuffer text, String value, final int equalIndex, final boolean active) {

		if (value != null) {

			// Escape potential leading spaces.
			if (value.startsWith(" ")) { //$NON-NLS-1$
				value = "\\" + value; //$NON-NLS-1$
			}

			final int lineLength = config.getWrapLineLength() - 1;
			int valueStartPos = equalIndex;
			if (config.isSpacesAroundEqualsEnabled()) {
				valueStartPos += 3;
			} else {
				valueStartPos += 1;
			}

			// Break line after escaped new line
			if (config.isNewLineNice()) {
				value = value.replaceAll("(\\\\r\\\\n|\\\\r|\\\\n)", //$NON-NLS-1$
						"$1\\\\" + SYSTEM_LINE_SEP); //$NON-NLS-1$
			}

			/*
			 * Do not wrap text when it contains "http://..." because this can very likely break a
			 * long url.
			 */
			final boolean isHttp = value.indexOf("http://") != -1 || value.indexOf("https://") != -1;

			if (isHttp) {

				/*
				 * This is not yet fully implemented compared with the line break algorithm, all
				 * lines now begin at the line start.
				 */

				saveValue(text, value);

			} else {

				// Wrap lines
				if (config.isWrapLinesEnabled() && valueStartPos < lineLength) {

					final StringBuffer valueBuf = new StringBuffer(value);

					while (valueBuf.length() + valueStartPos > lineLength || valueBuf.indexOf("\n") != -1) { //$NON-NLS-1$

						int endPos = Math.min(valueBuf.length(), lineLength - valueStartPos);
						final String line = valueBuf.substring(0, endPos);

						int breakPos = line.indexOf(SYSTEM_LINE_SEP);
						if (breakPos != -1) {

							endPos = breakPos + SYSTEM_LINE_SEP.length();
							saveValue(text, valueBuf.substring(0, endPos));
							//text.append(valueBuf.substring(0, endPos));

						} else {

							breakPos = line.lastIndexOf(' ');

							if (breakPos != -1) {
								endPos = breakPos + 1;
								saveValue(text, valueBuf.substring(0, endPos));
								//text.append(valueBuf.substring(0, endPos));
								text.append("\\"); //$NON-NLS-1$
								text.append(SYSTEM_LINE_SEP);
							}
						}

						valueBuf.delete(0, endPos);

						// Figure out starting position for next line
						if (!config.isWrapAlignEqualsEnabled()) {
							valueStartPos = config.getWrapIndentLength();
						}

						if (!active && valueStartPos > 0) {
							text.append("##"); //$NON-NLS-1$
						}

						for (int i = 0; i < valueStartPos; i++) {
							text.append(' ');
						}
					}

					text.append(valueBuf);

				} else {

					saveValue(text, value);
					//text.append(value);
				}
			}
		}
	}

	/**
	 * Converts unicodes to encoded &#92;uxxxx.
	 * 
	 * @param str
	 *            string to convert
	 * @return converted string
	 * @see java.util.Properties
	 */
	private String convertUnicodeToEncoded(final String str) {
		final int len = str.length();
		final StringBuffer outBuffer = new StringBuffer(len * 2);

		for (int x = 0; x < len; x++) {
			final char aChar = str.charAt(x);
			if ((aChar < 0x0020) || (aChar > 0x007e)) {
				outBuffer.append('\\');
				outBuffer.append('u');
				outBuffer.append(toHex((aChar >> 12) & 0xF));
				outBuffer.append(toHex((aChar >> 8) & 0xF));
				outBuffer.append(toHex((aChar >> 4) & 0xF));
				outBuffer.append(toHex(aChar & 0xF));
			} else {
				outBuffer.append(aChar);
			}
		}
		return outBuffer.toString();
	}

	/**
	 * Gets the position where the equal sign should be located for the given group.
	 * 
	 * @param key
	 *            resource bundle key
	 * @param group
	 *            resource bundle key group
	 * @param messagesBundle
	 *            resource bundle
	 * @return position
	 */
	private int getEqualIndex(final String key, final String group, final MessagesBundle messagesBundle) {
		int equalIndex = -1;
		final boolean alignEquals = config.isAlignEqualsEnabled();
		final boolean groupKeys = config.isGroupKeysEnabled();
		final boolean groupAlignEquals = config.isGroupAlignEqualsEnabled();

		// Exit now if we are not aligning equals
		if (!alignEquals || groupKeys && !groupAlignEquals || groupKeys && group == null) {
			return key.length();
		}

		// Get equal index
		final String[] keys = messagesBundle.getKeys();
		for (final String iterKey : keys) {
			if (!groupKeys || groupAlignEquals && iterKey.startsWith(group)) {
				final int index = iterKey.length();
				if (index > equalIndex) {
					equalIndex = index;
				}
			}
		}
		return equalIndex;
	}

	/**
	 * Gets the group from a resource bundle key.
	 * 
	 * @param key
	 *            the key to get a group from
	 * @return key group
	 */
	private String getKeyGroup(final String key) {
		final String sep = config.getGroupLevelSeparator();
		final int depth = config.getGroupLevelDepth();
		int endIndex = 0;
		int levelFound = 0;

		for (int i = 0; i < depth; i++) {
			final int sepIndex = key.indexOf(sep, endIndex);
			if (sepIndex != -1) {
				endIndex = sepIndex + 1;
				levelFound++;
			}
		}
		if (levelFound != 0) {
			if (levelFound < depth) {
				return key;
			}
			return key.substring(0, endIndex - 1);
		}
		return null;
	}

	private void saveKey(final StringBuffer buf, final String str) {
		saveText(buf, str, SPECIAL_KEY_SAVE_CHARS);
	}

	/**
	 * Saves some text in a given buffer after converting special characters.
	 * 
	 * @param buf
	 *            the buffer to store the text into
	 * @param str
	 *            the value to save
	 * @param escapeChars
	 *            characters to escape
	 */
	private void saveText(final StringBuffer buf, final String str, final String escapeChars) {

		final int len = str.length();

		for (int x = 0; x < len; x++) {

			final char aChar = str.charAt(x);
			if (escapeChars.indexOf(aChar) != -1) {
				buf.append('\\');
			}
			buf.append(aChar);
		}
	}

	private void saveValue(final StringBuffer buf, final String str) {
		saveText(buf, str, SPECIAL_VALUE_SAVE_CHARS);
	}

	/**
	 * Serializes a given <code>MessagesBundle</code> into a formatted string. The returned string
	 * will conform to documented properties file structure.
	 * 
	 * @param messagesBundle
	 *            the bundle used to generate the string
	 * @return the generated string
	 */
	public String serialize(final MessagesBundle messagesBundle) {
		final String lineBreak = SYSTEM_LINE_SEP;
		final int numOfLineBreaks = config.getGroupSepBlankLineCount();
		final StringBuffer text = new StringBuffer();

		// Header comment
		final String headComment = messagesBundle.getComment();
		if (config.isShowSupportEnabled() && !headComment.startsWith(GENERATED_BY)) {
			text.append(GENERATED_BY);
			text.append(SYSTEM_LINE_SEP);
		}
		if (headComment != null && headComment.length() > 0) {
			text.append(headComment);
		}

		// Format
		String group = null;
		int equalIndex = -1;
		final String[] keys = messagesBundle.getKeys();
		if (config.isKeySortingEnabled()) {
			Arrays.sort(keys);
		}
		for (final String key2 : keys) {
			String key = key2;
			final Message message = messagesBundle.getMessage(key);
			String value = message.getValue();
			final String comment = message.getComment();

			if (value != null) {
				// escape backslashes
				if (config.isUnicodeEscapeEnabled()) {
					value = value.replaceAll("\\\\", "\\\\\\\\");//$NON-NLS-1$ //$NON-NLS-2$
				}

				// handle new lines in value
				final String lineStyleCh = FORCED_LINE_SEP[config.getNewLineStyle()];
				if (lineStyleCh != null) {
					value = value.replaceAll("\r\n|\r|\n", lineStyleCh); //$NON-NLS-1$
				} else {
					value = value.replaceAll("\r", "\\\\r"); //$NON-NLS-1$ //$NON-NLS-2$
					value = value.replaceAll("\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				value = ""; //$NON-NLS-1$
			}

			//TODO Put check here and add to config: keep empty values?
			//default being false

			// handle group equal align and line break options
			if (config.isGroupKeysEnabled()) {
				final String newGroup = getKeyGroup(key);
				if (newGroup == null || !newGroup.equals(group)) {
					group = newGroup;
					equalIndex = getEqualIndex(key, group, messagesBundle);
					for (int j = 0; j < numOfLineBreaks; j++) {
						text.append(lineBreak);
					}
				}
			} else {
				equalIndex = getEqualIndex(key, null, messagesBundle);
			}

			// Build line
			if (config.isUnicodeEscapeEnabled()) {
				key = convertUnicodeToEncoded(key);
				value = convertUnicodeToEncoded(value);
			}
			if (comment != null && comment.length() > 0) {
				text.append(comment);
			}
			appendKey(text, key, equalIndex, message.isActive());
			appendValue(text, value, equalIndex, message.isActive());
			text.append(lineBreak);
		}
		return text.toString();
	}

	/**
	 * Converts a nibble to a hex character
	 * 
	 * @param nibble
	 *            the nibble to convert.
	 * @return a converted character
	 */
	private char toHex(final int nibble) {
		final char hexChar = HEX_DIGITS[(nibble & 0xF)];
		if (!config.isUnicodeEscapeUppercase()) {
			return Character.toLowerCase(hexChar);
		}
		return hexChar;
	}
}