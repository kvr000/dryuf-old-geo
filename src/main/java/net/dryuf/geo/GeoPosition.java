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

import net.dryuf.trans.meta.DynamicDefaults;
import net.dryuf.trans.meta.NoDynamic;


public class GeoPosition extends java.lang.Object
{
	protected long			created;

	public long			getCreated()
	{
		return this.created;
	}

	public void			setCreated(long created)
	{
		this.created = created;
	}

	protected int			lng;

	public int			getLng()
	{
		return this.lng;
	}

	public void			setLng(int lng_)
	{
		this.lng = lng_;
	}

	protected int			lat;

	public int			getLat()
	{
		return this.lat;
	}

	public void			setLat(int lat_)
	{
		this.lat = lat_;
	}

	protected int			alt;

	public int			getAlt()
	{
		return this.alt;
	}

	public void			setAlt(int alt_)
	{
		this.alt = alt_;
	}

	@NoDynamic
	public				GeoPosition()
	{
	}

	@NoDynamic
	public				GeoPosition(int lng, int lat, int alt)
	{
		this.lng = lng;
		this.lat = lat;
		this.alt = alt;
	}

	@DynamicDefaults(defaults = { "0", "0", "0", "0" })
	public				GeoPosition(int lng, int lat, int alt, long created)
	{
		this.lng = lng;
		this.lat = lat;
		this.alt = alt;
		this.created = created;
	}

	public GeoPosition		cloneWithoutAlt()
	{
		return new GeoPosition(lng, lat, 0, created);
	}

	public void			copyFullFrom(GeoPosition pos)
	{
		created = pos.created;
		copyPositionFrom(pos);
	}

	public void			copyPositionFrom(GeoPosition pos)
	{
		lat = pos.lat;
		lng = pos.lng;
		alt = pos.alt;
	}

	public double			computeLlDiffTo(GeoPosition second)
	{
		return Wgs84Util.computeLlaDiff(second.lng/10000000.0, second.lat/10000000.0, 0, lng/10000000.0, lat/10000000.0, 0);
	}

	public double			computeLlaDiffTo(GeoPosition second)
	{
		return Wgs84Util.computeLlaDiff(second.lng/10000000.0, second.lat/10000000.0, second.alt, lng/10000000.0, lat/10000000.0, alt);
	}

	public boolean			equals(Object so)
	{
		if (!(so instanceof GeoPosition))
			return false;
		GeoPosition s = (GeoPosition)so;
		return s.created == this.created && s.lng == this.lng && s.lat == this.lat && s.alt == this.alt;
	}

	public int			hashCode()
	{
		return (((((int) created*37)+lng)*37+lat)*37)+alt;
	}
}
