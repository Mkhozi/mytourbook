/*******************************************************************************
 * Copyright (C) 2005, 2014  Wolfgang Schramm and Contributors
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *******************************************************************************/
/**
 * @author Alfred Barten
 */
package net.tourbook.common.color;

import net.tourbook.common.Messages;
import net.tourbook.common.util.StatusUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

public class RGBVertex implements Comparable<Object>, Cloneable {

	private int			_value;
	private RGB			_rgb;

	/**
	 * Opacity 0.0 ... 1.0, default is 1.0 to ensure that color is visible.
	 */
	private float		_opacity	= 1.0f;

	/**
	 * Created id is needed that vertices can be sorted correctly when the value of two vertices are
	 * the same. This occures when new vertices are created and the value is not yet set.
	 */
	private int			_sortId;

	private static int	_sortIdCreator;

	public RGBVertex() {

		setSortId();
	}

	public RGBVertex(final int sortId) {

		_sortId = sortId;

		_value = 0;
		_rgb = new RGB(255, 255, 255); // WHITE
	}

	public RGBVertex(final int value, final int red, final int green, final int blue) {

		setSortId();

		if ((red > 255) || (red < 0) || (green > 255) || (green < 0) || (blue > 255) || (blue < 0)) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}

		_value = value;
		_rgb = new RGB(red, green, blue);
	}

	public RGBVertex(final int value, final int red, final int green, final int blue, final float opacity) {

		this(value, red, green, blue);

		_opacity = opacity;
	}

	public RGBVertex(final int value, final RGB rgb, final float opacity) {

		setSortId();

		_value = value;
		_rgb = rgb;
		_opacity = opacity;
	}

	public RGBVertex(final RGB rgb) {

		setSortId();

		_value = 0;
		_rgb = rgb;
	}

	@Override
	public RGBVertex clone() {

		RGBVertex clonedObject = null;

		try {

			clonedObject = (RGBVertex) super.clone();

			setSortId();

			clonedObject._rgb = new RGB(_rgb.red, _rgb.green, _rgb.blue);

		} catch (final CloneNotSupportedException e) {
			StatusUtil.log(e);
		}

		return clonedObject;
	}

	public int compareTo(final Object anotherObject) throws ClassCastException {

		if (!(anotherObject instanceof RGBVertex)) {
			throw new ClassCastException(Messages.rgv_vertex_class_cast_exception);
		}

		final RGBVertex anotherRGBVertex = (RGBVertex) anotherObject;
		final int anotherValue = anotherRGBVertex.getValue();

		if (_value == anotherValue) {

			// both values are the same, compare/sort by create id

			return _sortId - anotherRGBVertex._sortId;

		} else {

			return _value - anotherValue;
		}
	}

	public float getOpacity() {
		return _opacity;
	}

	public RGB getRGB() {
		return _rgb;
	}

	public int getSortId() {
		return _sortId;
	}

	public int getValue() {
		return _value;
	}

	public void setOpacity(final float opacity) {
		_opacity = opacity;
	}

	public void setRGB(final RGB rgb) {
		_rgb = rgb;
	}

	/**
	 * @return Returns a unique id.
	 */
	private void setSortId() {

		_sortId = ++_sortIdCreator;
	}

	public void setValue(final int value) {
		_value = value;
	}

	@Override
	public String toString() {
		return String.format("\n\tRGBVertex [_value=%s, _rgb=%s]", _value, _rgb); //$NON-NLS-1$
	}
}
