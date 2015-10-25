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

package net.dryuf.geo.resolve.nominatim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;

import net.dryuf.geo.resolve.GeocodeResolver;
import net.dryuf.sql.SqlHelper;
import net.dryuf.core.StringUtil;
import net.dryuf.util.MapUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;


public class NominatimGeocodeResolver extends java.lang.Object implements GeocodeResolver
{
	public NominatimGeocodeResolver()
	{
	}

	public String			lookupGeocode(int lng, int lat)
	{
		Connection dbConnection = null;
		try {
			dbConnection = nominatimDataSource.getConnection();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		try {
			// Prefered language
			List<String> languagePreference = Arrays.asList(new String[]{ "cs", "en" });
			String languagePreferenceSql = "ARRAY["+StringUtil.joinEscaped((String input) -> "?", ",", languagePreference)+"]";

			PreparedStatement lookupMain = dbConnection.prepareStatement(
					"SELECT place_id, parent_place_id FROM placex\n"+
					"WHERE\n"+
					"	ST_DWithin(ST_SetSRID(ST_Point(?, ?), 4326), geometry, ?)\n"+
					"	AND rank_search != 28 AND rank_search >= ?\n"+
					"	AND (name IS NOT NULL OR housenumber IS NOT NULL)\n"+
					"	AND class NOT IN (\"waterway\")\n"+
					"	AND (ST_GeometryType(geometry) NOT IN (\"ST_Polygon\",\"ST_MultiPolygon\")\n"+
					"	OR ST_DWithin(ST_SetSRID(ST_Point(?, ?), 4326), ST_Centroid(geometry), ?))\n"+
					"ORDER BY\n"+
					"	ST_distance(ST_SetSRID(ST_Point(?, ?), 4326), geometry) ASC LIMIT 1\n"
			);

			for (int i = 0; i < 1; i++) {
				// Show address breakdown
				boolean showAddressDetails = true;
				//if (isset(_GET.getAddressdetails())) showAddressDetails = (bool)_GET["addressdetails"];

				// Location to look up
				double lngDouble = lng/10000000.0;
				double latDouble = lat/10000000.0;
				String pointSQL = "ST_SetSRID(ST_Point(?, ?), 4326)";

				// Zoom to rank, this could probably be calculated but a lookup gives fine control
				int maxRank = 28; //isset(aZoomRank[null])?aZoomRank[null]:28;

				// Find the nearest point
				double searchDiameter = 0.0001;
				Object placeId = null;
				boolean area = false;
				float maxAreaDistance = 1;

				Map<String, Object> place = null;
				while (placeId == null && searchDiameter < maxAreaDistance) {
					searchDiameter = searchDiameter * 2;

					// If we have to expand the search area by a large amount then we need a larger feature
					// then there is a limit to how small the feature should be
					if (searchDiameter > 2 && maxRank > 4) maxRank = 4;
					if (searchDiameter > 1 && maxRank > 9) maxRank = 8;
					if (searchDiameter > 0.8 && maxRank > 10) maxRank = 10;
					if (searchDiameter > 0.6 && maxRank > 12) maxRank = 12;
					if (searchDiameter > 0.2 && maxRank > 17) maxRank = 17;
					if (searchDiameter > 0.1 && maxRank > 18) maxRank = 18;
					if (searchDiameter > 0.008 && maxRank > 22) maxRank = 22;
					if (searchDiameter > 0.001 && maxRank > 26) maxRank = 26;

					if ((place = SqlHelper.executeRow(lookupMain, new Object[]{ lngDouble, latDouble, searchDiameter, maxRank, lngDouble, latDouble, searchDiameter, lngDouble, latDouble })) == null) {
						continue;
						//throw new net.dryuf.Exception("Could not determine closest place."+ sql);
					}
					//System.err.print(place);
					placeId = place.get("place_id");
				}

				// The point we found might be too small - use the address to find what it is a child of
				if (placeId != null) {
					String sql = "select address_place_id from place_addressline where cached_rank_address <= ? and place_id = ? order by cached_rank_address desc,isaddress desc,distance desc limit 1";
					if ((placeId = SqlHelper.queryColumn(dbConnection, sql, new Object[]{ maxRank, placeId })) == null) {
						throw new RuntimeException("Could not get parent for place.");
					}

					if (placeId != null && place.get("place_id") != null && maxRank < 28) {
						sql = "select address_place_id from place_addressline where cached_rank_address <= ? and place_id = ? order by cached_rank_address desc,isaddress desc,distance desc limit 1";
						if ((placeId = SqlHelper.queryColumn(dbConnection, sql, new Object[] { maxRank, place.get("place_id") })) == null) {
							throw new RuntimeException("Could not get larger parent for place.");
						}
					}
				}

				if (placeId != null) {
					String sql = "select placex.*,\n"+
						"get_address_by_language(place_id, "+languagePreferenceSql+") as langaddress,\n"+
						"get_name_by_language(name, "+languagePreferenceSql+") as placename,\n"+
						"get_name_by_language(name, ARRAY['ref']) as ref,\n"+
						"st_y(st_centroid(geometry)) as lat, st_x(st_centroid(geometry)) as lon\n"+
						"from placex where place_id = ?";
					List<Object> binds = new LinkedList<Object>();
					binds.addAll(languagePreference);
					binds.addAll(languagePreference);
					binds.add(placeId);
					if ((place = SqlHelper.queryRow(dbConnection, sql, binds.toArray())) == null) {
						throw new RuntimeException("bad place");
					}
				}
			}
		}
		catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
		finally {
			try {
				dbConnection.close();
			}
			catch (SQLException ex) {
			}
		}
		return null;
	}

	@Inject
	@Named("javax.sql.DataSource-nominatim")
	DataSource			nominatimDataSource;

	protected static Map<Integer, Integer> zoomRank = MapUtil.createHashMap(
				0, 2, // Continent / Sea
				1, 2,
				2, 2,
				3, 4, // Country
				4, 4,
				5, 8, // State
				6, 10, // Region
				7, 10,
				8, 12, // County
				9, 12,
				10, 17, // City
				11, 17,
				12, 18, // Town / Village
				13, 18,
				14, 22, // Suburb
				15, 22,
				16, 26, // Street, major street?
				17, 26,
				18, 30, // or >, Building
				19, 30 // or >, Building
			);
}
