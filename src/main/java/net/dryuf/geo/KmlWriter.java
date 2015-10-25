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


public class KmlWriter extends java.lang.Object
{
	public static final String	KML_URI = "http://www.opengis.net/kml/2.2";

	public				KmlWriter(XMLStreamWriter stream)
	{
		this.stream = stream;
	}

	public KmlWriter		endElement() throws XMLStreamException
	{
		stream.writeEndElement();
		return this;
	}

	public KmlWriter		startKml() throws XMLStreamException
	{
		stream.writeStartDocument();
		stream.writeStartElement("kml");
		stream.writeDefaultNamespace(KML_URI);
		return this;
	}

	public KmlWriter		endKml() throws XMLStreamException
	{
		endElement();
		stream.close();
		return this;
	}

	public KmlWriter		startDocument() throws XMLStreamException
	{
		stream.writeStartElement(KML_URI, "Document");
		return this;
	}

	public KmlWriter		startPlacemark() throws XMLStreamException
	{
		stream.writeStartElement(KML_URI, "Placemark");
		return this;
	}

	public KmlWriter		writeLineString(Collection<GeoPosition> positions) throws XMLStreamException
	{
		stream.writeStartElement(KML_URI, "LineString");
		stream.writeStartElement(KML_URI, "coordinates");
		stream.writeCharacters("\n");
		for (GeoPosition position: positions) {
			stream.writeCharacters(String.format(Locale.ENGLISH, "%.7g,%.7g,%d\n", position.lng/10000000.0, position.lat/10000000.0, position.alt));
		}
		stream.writeEndElement();
		stream.writeEndElement();
		return this;
	}

	public XMLStreamWriter		stream;
};
