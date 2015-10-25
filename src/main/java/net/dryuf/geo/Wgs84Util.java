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

import net.dryuf.trans.meta.Out;


public class Wgs84Util extends java.lang.Object
{
	protected static double		EARTH_A;
	protected static double		EARTH_B;
	protected static double		EARTH_F;
	protected static double		EARTH_Ecc;
	protected static double		EARTH_Esq;
	protected static double		EARTH_FE;
	protected static double		EARTH_ONE_M_FE;
	protected static double		EARTH_E2;
	protected static double		EARTH_EPSILON;

	static {
		EARTH_A		= 6378137;
		EARTH_F		= 1.0/298.257223563;
		EARTH_B		= EARTH_A*(1.0-EARTH_F);

		EARTH_Esq	= 1-EARTH_B*EARTH_B/(EARTH_A*EARTH_A);
		EARTH_Ecc	= Math.sqrt(EARTH_Esq);

		EARTH_FE	=  1.0/298.257223563;
		EARTH_ONE_M_FE	=  1.0 - EARTH_FE;

		EARTH_E2	= EARTH_FE * (2.0 - EARTH_FE); // first eccentricity squared
		EARTH_EPSILON	= EARTH_E2 / (1.0 - EARTH_E2); // second eccentricity squared
	}

	public static void		convertLlaToXyz(@Out double[] xyz, double lng, double lat, double alt)
	{
		double slat, clat, slng, clng;
		slat = Math.sin(lat*(Math.PI/180)); clat = Math.cos(lat*(Math.PI/180));
		slng = Math.sin(lng*(Math.PI/180)); clng = Math.cos(lng*(Math.PI/180));

		double dsq =  1.0-EARTH_Esq*slat*slat;
		double rn =  EARTH_A/Math.sqrt(dsq);

		xyz[0] = (rn+alt)*clat*clng;
		xyz[1] = (rn+alt)*clat*slng;
		xyz[2] = ((1-EARTH_Esq)*rn+alt)*slat;
	}

	public void			convertXyzToLla(@Out double[] lla, double[] xyz)
	{
		double x = xyz[0];
		double y = xyz[1];
		double z = xyz[2];

		double p;
		// quick check for all components zero+
		if (x == 0 && y == 0 && z == 0) {
		}
		// quick calculations at poles+
		else if (x == 0 && y == 0 && z != 0) {
			lla[0] = 0;
			lla[1] = ((z<0)?-1:1) * Math.PI / 2;
			lla[2] = Math.abs(z) - EARTH_B;
		}
		// quick calculations at equator+
		else if (z == 0.0) {
			lla[0] = Math.atan2(y, x);
			lla[1] = 0;
			p = Math.sqrt(x*x + y*y);
			lla[2] = p - EARTH_A;
		}
		// main algorithm+ in bowring (1985), u is the parametric latitude+ it is crucial
		// to maintain the appropriate signs for the sin(u) and sin(lat) in the equations
		// below+
		else {
			double p2 = x*x + y*y;
			double r2 = p2 + z*z;
			p = Math.sqrt(p2);
			double r = Math.sqrt(r2);

			// equation (17) from bowring (1985), shown to improve numerical accuracy in lat
			double tanu = EARTH_ONE_M_FE * (z / p) * (1 + (EARTH_EPSILON * EARTH_B) / r);
			double tan2u = tanu * tanu;

			// avoid trigonometric publics for determining cos3u and sin3u
			double cos2u = 1.0 / (1.0 + tan2u);
			double cosu = Math.sqrt(cos2u);
			double cos3u = cos2u * cosu;

			double sinu = tanu * cosu;
			double sin2u = 1+0 - cos2u;
			double sin3u = sin2u * sinu;

			// equation (18) from bowring (1985)
			double tanlat = (z + EARTH_EPSILON * EARTH_B * sin3u) / (p - EARTH_E2 * EARTH_A * cos3u);

			double tan2lat = tanlat * tanlat;
			double cos2lat = 1+0 / (1+0 + tan2lat);
			double sin2lat = 1+0 - cos2lat;

			double coslat = Math.sqrt(cos2lat);
			double sinlat = tanlat * coslat;

			lla[0] = Math.atan2(y, x);
			lla[1] = Math.atan(tanlat);

			// equation (7) from bowring (1985), shown to be numerically superior to other
			// height equations+ note that equation (7) from bowring (1985) writes the last
			// term as a^2 / nu, but this reduces to a * sqrt(1 - e^2 * sin(lat)^2), because
			// nu = a / sqrt(1 - e^2 * sin(lat)^2)+
			lla[2] = p * coslat + z * sinlat - EARTH_A * Math.sqrt(1+0 - EARTH_E2 * sin2lat);
		}

		// convert outputs if necessary+
		lla[0] *= 180/Math.PI;
		lla[1] *= 180/Math.PI;
	}


	public static double		computeLlaDiff(double lng0, double lat0, double alt0, double lng1, double lat1, double alt1)
	{
		double[] p0 = new double[3];
		double[] p1 = new double[3];
		convertLlaToXyz(p0, lng0, lat0, alt0);
		convertLlaToXyz(p1, lng1, lat1, alt1);
		return Math.sqrt((p0[0]-p1[0])*(p0[0]-p1[0])+(p0[1]-p1[1])*(p0[1]-p1[1])+(p0[2]-p1[2])*(p0[2]-p1[2]));
	}

	public static double		computeLlDiff(double lng0, double lat0, double lng1, double lat1)
	{
		double[] p0 = new double[3];
		double[] p1 = new double[3];
		convertLlaToXyz(p0, lng0, lat0, 0);
		convertLlaToXyz(p1, lng1, lat1, 0);
		return Math.sqrt((p0[0]-p1[0])*(p0[0]-p1[0])+(p0[1]-p1[1])*(p0[1]-p1[1])+(p0[2]-p1[2])*(p0[2]-p1[2]));
	}
}
