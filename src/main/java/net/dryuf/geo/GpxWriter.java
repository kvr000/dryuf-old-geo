/*
 * Dryuf framework
 *
 * ----------------------------------------------------------------------------------
 *
 * Copyright (C) 2000-2015 Zbyněk Vyškovský
 *
 * ----------------------------------------------------------------------------------
 *
 * LICENSE:
 *
 * This file is part of Dryuf
 *
 * Dryuf is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * Dryuf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Dryuf; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * @author	2000-2015 Zbyněk Vyškovský
 * @link	mailto:kvr@matfyz.cz
 * @link	http://kvr.matfyz.cz/software/java/dryuf/
 * @link	http://github.com/dryuf/
 * @license	http://www.gnu.org/licenses/lgpl.txt GNU Lesser General Public License v3
 */

package net.dryuf.geo;

import java.util.Collection;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.dryuf.time.util.DateTimeUtil;


public class GpxWriter extends java.lang.Object
{
	public static final String	GPX_URI = "http://www.topografix.com/GPX/1/1";

	/**
	 * Date format for a point timestamp.
	 */
	public				GpxWriter(XMLStreamWriter stream)
	{
		this.stream = stream;
	}

	public GpxWriter		endElement() throws XMLStreamException
	{
		stream.writeEndElement();
		return this;
	}

	public GpxWriter		startGpx() throws XMLStreamException
	{
		stream.writeStartDocument();
		stream.writeStartElement("gpx");
		stream.writeDefaultNamespace(GPX_URI);
		stream.writeAttribute("version", "1.1");
		return this;
	}

	public GpxWriter		endGpx() throws XMLStreamException
	{
		endElement();
		stream.close();
		return this;
	}

	public GpxWriter		writeTrack(Collection<GeoPosition> positions) throws XMLStreamException
	{
		stream.writeStartElement(GPX_URI, "trk");
		stream.writeStartElement(GPX_URI, "trkseg");
		stream.writeCharacters("\n");
		for (GeoPosition position: positions) {
			stream.writeStartElement(GPX_URI, "trkpt");
			stream.writeAttribute("lon", String.format(Locale.ENGLISH, "%.7g", position.lng/10000000.0));
			stream.writeAttribute("lat", String.format(Locale.ENGLISH, "%.7g", position.lat/10000000.0));
			stream.writeStartElement(GPX_URI, "time"); stream.writeCharacters(DateTimeUtil.formatUtcIso(position.created)); stream.writeEndElement();
			stream.writeEndElement();
			stream.writeCharacters("\n");
		}
		stream.writeEndElement();
		stream.writeEndElement();
		return this;
	}

	public XMLStreamWriter		stream;
}
