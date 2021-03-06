/*******************************************************************************
 * Copyright (C) 2005, 2010  Wolfgang Schramm and Contributors
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
<<<<<<< HEAD:net.tourbook.device.polar.hrm/src/net/tourbook/device/polar/hrm/Tour.java
 * You should have received a copy of the GNU General Public License along with 
=======
 * You should have received a copy of the GNU General Public License along with
>>>>>>> 14432ce3ac8069f89f4901c9bf0eb194c103fb49:net.tourbook.device/src/net/tourbook/device/InvalidDeviceSAXException.java
 * this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *******************************************************************************/
package net.tourbook.device;

import org.xml.sax.SAXException;

/**
 * This exception is thrown when import data are not valid for the requested device.
 */
public class InvalidDeviceSAXException extends SAXException {

	private static final long	serialVersionUID	= -219904266152289685L;

	public InvalidDeviceSAXException(final String message) {
		super(message);
	}

}
