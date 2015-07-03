package org.mtransit.parser.ca_strathcona_county_transit_bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStopTime;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;

// https://data.strathcona.ca/
// https://data.strathcona.ca/Transportation/Strathcona-County-Transit-Bus-Schedule-GFTS-Data-F/cvta-prr6
// https://data.strathcona.ca/download/cvta-prr6/application/zip
public class StrathconaCountyTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-strathcona-county-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new StrathconaCountyTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("Generating Strathcona County Transit bus data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("Generating Strathcona County Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	private static final String A = "A";
	private static final String B = "B";

	private static final long RID_EW_A = 10000;
	private static final long RID_EW_B = 20000;

	@Override
	public long getRouteId(GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.route_id)) {
			return Long.parseLong(gRoute.route_id);
		}
		Matcher matcher = DIGITS.matcher(gRoute.route_id);
		matcher.find();
		long id = Long.parseLong(matcher.group());
		if (gRoute.route_id.endsWith(A)) {
			return RID_EW_A + id;
		} else if (gRoute.route_id.endsWith(B)) {
			return RID_EW_B + id;
		}
		System.out.println("Unexpected route ID " + gRoute);
		System.exit(-1);
		return -1l;
	}

	private static final String BTT = "Bethel TT";
	private static final String TC = "Ordze TC";
	private static final String DAB = "Dial-A-Bus";
	private static final String EDM_CITY_CTR = "Edm City Ctr";
	private static final String GOV_CTR = "Gov Ctr";
	private static final String NAIT = "NAIT";
	private static final String NAIT_GOV_CTR = NAIT + " / " + GOV_CTR;
	private static final String GOV_CTR_NAIT = GOV_CTR + " / " + NAIT;
	private static final String U_OF_ALBERTA = "U of Alberta";
	private static final String STRATHMOOR_DRIVE = "Strathmoor Dr";
	private static final String MILLENNIUM_PLACE = "Millennium Pl";
	private static final String EMERALD_HILLS = "Emerald Hls";
	private static final String ABJ = "ABJ Sch";
	private static final String EMERALD_HILLS_ABJ = EMERALD_HILLS + " / " + ABJ;
	private static final String SUMMERWOOD = "Summerwood";
	private static final String CLARKDALE = "Clarkdale";
	private static final String CLOVER_BAR = "Clover Bar";
	private static final String HERITAGE_HILLS = "Heritage Hls";
	private static final String NOTTINGHAM = "Nottingham";
	private static final String BRENTWOOD = "Brentwood";
	private static final String GLEN_ALLAN = "Glen Allan";
	private static final String OAK_ST = "Oak St";
	private static final String VILLAGE = "Village";
	private static final String BROADMOOR = "Broadmoor";
	private static final String CITP = "Ctr in the Park";

	private static final String RLN_401 = TC + " - " + EDM_CITY_CTR;
	private static final String RLN_403 = TC + " - " + GOV_CTR;
	private static final String RLN_404 = TC + " - " + U_OF_ALBERTA;
	private static final String RLN_411 = BTT + " - " + EDM_CITY_CTR;
	private static final String RLN_413 = BTT + " - " + GOV_CTR_NAIT;
	private static final String RLN_414 = BTT + " - " + U_OF_ALBERTA;
	private static final String RLN_420 = BTT + " - " + MILLENNIUM_PLACE;
	private static final String RLN_430 = BTT + " - " + EMERALD_HILLS + " CW";
	private static final String RLN_431 = BTT + " - " + EMERALD_HILLS + " CCW";
	private static final String RLN_432 = BTT + " -" + SUMMERWOOD;
	private static final String RLN_433 = BTT + " - " + CLARKDALE;
	private static final String RLN_433A = CLARKDALE + " - " + ABJ;
	private static final String RLN_440 = BTT + " - " + HERITAGE_HILLS;
	private static final String RLN_441 = BTT + " - " + TC + " - Regency";
	private static final String RLN_442 = BTT + " - " + NOTTINGHAM;
	private static final String RLN_443 = BTT + " - " + TC + " - Glen Allan";
	private static final String RLN_443A = BTT + " - " + BRENTWOOD;
	private static final String RLN_443B = BTT + " - " + GLEN_ALLAN;
	private static final String RLN_450 = BTT + " - " + CITP;
	private static final String RLN_451 = BTT + " - " + TC + " - Mills Haven / Westboro";
	private static final String RLN_451A = BTT + " - Woodbridge";
	private static final String RLN_451B = BTT + " - Mills Haven";
	private static final String RLN_490 = DAB + " A";
	private static final String RLN_491 = DAB + " B";
	private static final String RLN_492 = DAB + " C";
	private static final String RLN_493 = DAB + " D";
	private static final String RLN_494 = DAB + " E";
	private static final String RLN_495 = DAB + " F";

	@Override
	public String getRouteLongName(GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.route_short_name)) {
			// @formatter:off
			if (RSN_433A.equals(gRoute.route_short_name)) { return RLN_433A;
			} else if (RSN_443A.equals(gRoute.route_short_name)) { return RLN_443A;
			} else if (RSN_443B.equals(gRoute.route_short_name)) { return RLN_443B;
			} else if (RSN_451A.equals(gRoute.route_short_name)) { return RLN_451A;
			} else if (RSN_451B.equals(gRoute.route_short_name)) { return RLN_451B;
			// @formatter:on
			} else {
				System.out.println("Unexpected route long name " + gRoute);
				System.exit(-1);
				return null;
			}
		}
		int rsn = Integer.parseInt(gRoute.route_short_name);
		switch (rsn) {
		// @formatter:off
		case 401: return RLN_401;
		case 403: return RLN_403;
		case 404: return RLN_404;
		case 411: return RLN_411;
		case 413: return RLN_413;
		case 414: return RLN_414;
		case 420: return RLN_420;
		case 430: return RLN_430;
		case 431: return RLN_431;
		case 432: return RLN_432;
		case 433: return RLN_433;
		case 440: return RLN_440;
		case 441: return RLN_441;
		case 442: return RLN_442;
		case 443: return RLN_443;
		case 450: return RLN_450;
		case 451: return RLN_451;
		case 490: return RLN_490;
		case 491: return RLN_491;
		case 492: return RLN_492;
		case 493: return RLN_493;
		case 494: return RLN_494;
		case 495: return RLN_495;
		// @formatter:on
		default:
			System.out.println("Unexpected route long name " + gRoute);
			System.exit(-1);
			return null;
		}
	}

	private static final String AGENCY_COLOR_GREEN = "559820"; // GREEN (from map PDF)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String RSN_433A = "433A";
	private static final String RSN_443A = "443A";
	private static final String RSN_443B = "443B";
	private static final String RSN_451A = "451A";
	private static final String RSN_451B = "451B";

	private static final String COLOR_ED0E58 = "ED0E58";
	private static final String COLOR_231F20 = "231F20";
	private static final String COLOR_00A34E = "00A34E";
	private static final String COLOR_6E6EAB = "6E6EAB";
	private static final String COLOR_D04CAE = "D04CAE";
	private static final String COLOR_F78F20 = "F78F20";
	private static final String COLOR_9F237E = "9F237E";
	private static final String COLOR_FFC745 = "FFC745";
	private static final String COLOR_6BC7B9 = "6BC7B9";
	private static final String COLOR_0076BC = "0076BC";
	private static final String COLOR_F16278 = "F16278";
	private static final String COLOR_ED1C24 = "ED1C24";
	private static final String COLOR_FFF30C = "FFF30C";
	private static final String COLOR_08796F = "08796F";
	private static final String COLOR_652290 = "652290";
	private static final String COLOR_7BC928 = "7BC928";
	private static final String COLOR_832B30 = "832B30";
	private static final String COLOR_2E3192 = "2E3192";
	private static final String COLOR_006A2F = "006A2F";
	private static final String COLOR_EC008C = "EC008C";
	private static final String COLOR_F57415 = "F57415";
	private static final String COLOR_1270BB = "1270BB";
	private static final String COLOR_ED2D32 = "ED2D32";
	private static final String COLOR_0F6B3B = "0F6B3B";
	private static final String COLOR_61CACA = "61CACA";
	private static final String COLOR_E59A12 = "E59A12";

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.route_short_name)) {
			// @formatter:off
			if (RSN_433A.equals(gRoute.route_short_name)) { return COLOR_ED0E58;
			} else if (RSN_443A.equals(gRoute.route_short_name)) { return COLOR_231F20;
			} else if (RSN_443B.equals(gRoute.route_short_name)) { return COLOR_00A34E;
			} else if (RSN_451A.equals(gRoute.route_short_name)) { return COLOR_6E6EAB;
			} else if (RSN_451B.equals(gRoute.route_short_name)) { return COLOR_D04CAE;
			// @formatter:on
			} else {
				System.out.println("Unexpected route color " + gRoute);
				System.exit(-1);
				return null;
			}
		}
		int rsn = Integer.parseInt(gRoute.route_short_name);
		switch (rsn) {
		// @formatter:off
		case 401: return COLOR_F78F20;
		case 403: return COLOR_9F237E;
		case 404: return COLOR_FFC745;
		case 411: return COLOR_6BC7B9;
		case 413: return COLOR_0076BC;
		case 414: return COLOR_F16278;
		case 420: return COLOR_ED1C24;
		case 430: return COLOR_2E3192;
		case 431: return COLOR_FFF30C;
		case 432: return COLOR_08796F;
		case 433: return COLOR_652290;
		case 440: return COLOR_7BC928;
		case 441: return COLOR_832B30;
		case 442: return COLOR_2E3192;
		case 443: return COLOR_006A2F;
		case 450: return COLOR_EC008C;
		case 451: return COLOR_F57415;
		case 490: return COLOR_1270BB;
		case 491: return COLOR_ED2D32;
		case 492: return COLOR_0F6B3B;
		case 493: return COLOR_61CACA;
		case 494: return COLOR_E59A12;
		case 495: return null;
		// @formatter:on
		default:
			System.out.println("Unexpected route color " + gRoute);
			System.exit(-1);
			return null;
		}
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS.containsKey(mRoute.id)) {
			return; // split
		}
		if (mRoute.id == 443l + RID_EW_A) { // 443A
			if (gTrip.trip_headsign.equals("AM") || gTrip.trip_headsign.equals("START")) {
				mTrip.setHeadsignString("AM", 0);
				return;
			} else if (gTrip.trip_headsign.equals("PM") || gTrip.trip_headsign.equals("End")) {
				mTrip.setHeadsignString("PM", 1);
				return;
			}
			System.out.printf("Unexpected trip (route ID: %s): %s\n", mRoute.id, gTrip);
			System.exit(-1);
		} else if (mRoute.id == 490l) {
			if (gTrip.trip_headsign.equals("FULL")) {
				mTrip.setHeadsignString(ABJ, 0);
				return;
			} else if (gTrip.trip_headsign.equals("LAST")) {
				mTrip.setHeadsignString("Streambank Ave", 1);
				return;
			}
			System.out.printf("Unexpected trip (route ID: %s): %s\n", mRoute.id, gTrip);
			System.exit(-1);
		} else if (mRoute.id == 491l) {
			if (gTrip.trip_headsign.equals("FULL")) {
				mTrip.setHeadsignString("Davidson Dr & Darlington Dr", 0);
				return;
			} else if (gTrip.trip_headsign.equals("LAST")) {
				mTrip.setHeadsignString("Streambank Ave", 1);
				return;
			}
			System.out.printf("Unexpected trip (route ID: %s): %s\n", mRoute.id, gTrip);
			System.exit(-1);
		} else if (mRoute.id == 492l) {
			if (gTrip.trip_headsign.equals("FULL")) {
				mTrip.setHeadsignString("Highland Wy / Heritage Dr", 0);
				return;
			} else if (gTrip.trip_headsign.equals("LAST")) {
				mTrip.setHeadsignString("Streambank Ave", 1);
				return;
			}
			System.out.printf("Unexpected trip (route ID: %s): %s\n", mRoute.id, gTrip);
			System.exit(-1);
		} else if (mRoute.id == 493l) {
			if (gTrip.trip_headsign.equals("FULL")) {
				mTrip.setHeadsignString("Oak St / Sherwood Dr", 0);
				return;
			} else if (gTrip.trip_headsign.equals("LAST")) {
				mTrip.setHeadsignString("Streambank Ave", 1);
				return;
			}
			System.out.printf("Unexpected trip (route ID: %s): %s\n", mRoute.id, gTrip);
			System.exit(-1);
		} else if (mRoute.id == 494l) {
			if (gTrip.trip_headsign.equals("FULL")) {
				mTrip.setHeadsignString(TC, 0);
				return;
			} else if (gTrip.trip_headsign.equals("LAST")) {
				mTrip.setHeadsignString("Streambank Ave", 1);
				return;
			}
			System.out.printf("Unexpected trip (route ID: %s): %s\n", mRoute.id, gTrip);
			System.exit(-1);
		} else if (mRoute.id == 495l) {
			if (gTrip.trip_headsign.equals("FULL")) {
				mTrip.setHeadsignString("Sherwood Dr / Oak St", 0);
				return;
			} else if (gTrip.trip_headsign.equals("LAST")) {
				mTrip.setHeadsignString("Streambank Ave", 1);
				return;
			}
			System.out.printf("Unexpected trip (route ID: %s): %s\n", mRoute.id, gTrip);
			System.exit(-1);
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.trip_headsign), gTrip.direction_id);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = MSpec.cleanStreetTypes(tripHeadsign);
		return MSpec.cleanLabel(tripHeadsign);
	}

	private static final String DASH = "-";
	private static final String ALL = "*";

	private static final String STOP_ID_1223 = "1223"; // _106_ST_AND_117_AVE
	private static final String STOP_ID_1227 = "1227"; // _106_ST_AND_117_AVE
	private static final String STOP_ID_1732 = "1732"; // _107_ST_AND_104_AVE
	private static final String STOP_ID_1832 = "1832"; // _GREYHOUND_WB
	private static final String STOP_ID_1973 = "1973"; // _107_ST_AND_104_AVE
	private static final String STOP_ID_2001 = "2001"; // Festival Ln & Festival Ave
	private static final String STOP_ID_2636 = "2636"; // _107_U_TRANSIT_CTR
	private static final String STOP_ID_4000 = "4000"; // _ORDZE_TC
	private static final String STOP_ID_5040 = "5040"; // Village Dr & Village Dr
	private static final String STOP_ID_5041 = "5041"; // Kaska Rd at Hughe's
	private static final String STOP_ID_5115 = "5115"; // Village Dr & Village Dr
	private static final String STOP_ID_6029 = "6029"; // Main Blvd & Mission Dr
	private static final String STOP_ID_6048 = "6048"; // Oak St & Sherwood Dr
	private static final String STOP_ID_7199 = "7199"; // Highland Wy & Heritage Dr
	private static final String STOP_ID_7317 = "7317"; // Clarkdale Dr & Orchid Cr
	private static final String STOP_ID_7508 = "7508"; // Summerwood Blvd & Clover Bar Rd
	private static final String STOP_ID_7920 = "7920"; // ABJ School
	private static final String STOP_ID_7921 = "7921"; // ABJ School
	private static final String STOP_ID_8000 = "8000"; // _BTT
	private static final String STOP_ID_8113 = "8113"; // Jim Common Dr & Crystal Ln
	private static final String STOP_ID_8114 = "8114"; // Jim Common Dr & Crystal Ln
	private static final String STOP_ID_8810 = "8810"; // Strathmoor Dr
	private static final String STOP_ID_8811 = "8811"; // Strathmoor Dr
	private static final String STOP_ID_9015 = "9015"; // Nottingham Blvd & Nottingham Rd

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS;
	static {
		HashMap<Long, RouteTripSpec> map = new HashMap<Long, RouteTripSpec>();
		map.put(401l, new RouteTripSpec(401l, //
				0, MTrip.HEADSIGN_TYPE_STRING, TC, //
				1, MTrip.HEADSIGN_TYPE_STRING, EDM_CITY_CTR) //
				.addALLFromTo(0, STOP_ID_1832, STOP_ID_4000) //
				.addALLFromTo(1, STOP_ID_4000, STOP_ID_1832) //
		);
		map.put(403l, new RouteTripSpec(403l, //
				0, MTrip.HEADSIGN_TYPE_STRING, TC, //
				1, MTrip.HEADSIGN_TYPE_STRING, EDM_CITY_CTR) //
				.addALLFromTo(0, STOP_ID_1732, STOP_ID_4000) //
				.addALLFromTo(1, STOP_ID_4000, STOP_ID_1973) //
		);
		map.put(404l, new RouteTripSpec(404l, //
				0, MTrip.HEADSIGN_TYPE_STRING, TC, //
				1, MTrip.HEADSIGN_TYPE_STRING, U_OF_ALBERTA) //
				.addALLFromTo(0, STOP_ID_2636, STOP_ID_4000) //
				.addALLFromTo(1, STOP_ID_4000, STOP_ID_2636) //
		);
		map.put(411l, new RouteTripSpec(411l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, EDM_CITY_CTR) //
				.addALLFromTo(0, STOP_ID_1973, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_1973) //
		);
		map.put(413l, new RouteTripSpec(413l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, NAIT_GOV_CTR) //
				.addALLFromTo(0, STOP_ID_1223, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_1227) //
		);
		map.put(414l, new RouteTripSpec(414l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, U_OF_ALBERTA) //
				.addALLFromTo(0, STOP_ID_2636, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_2636) //
		);
		map.put(420l, new RouteTripSpec(420l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, STRATHMOOR_DRIVE) //
				.addALLFromTo(0, STOP_ID_8811, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_8810) //
		);
		map.put(430l, new RouteTripSpec(430l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, EMERALD_HILLS_ABJ) //
				.addALLFromTo(0, STOP_ID_7921, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_7921) //
		);
		map.put(431l, new RouteTripSpec(431l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, EMERALD_HILLS_ABJ) //
				.addALLFromTo(0, STOP_ID_7920, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_7920) //
		);
		map.put(432l, new RouteTripSpec(432l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, SUMMERWOOD) //
				.addALLFromTo(0, STOP_ID_7508, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_7508) //
		);
		map.put(433l, new RouteTripSpec(433l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, SUMMERWOOD) //
				.addALLFromTo(0, STOP_ID_7317, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_7317) //
		);
		map.put(433l + RID_EW_A, new RouteTripSpec(433l + RID_EW_A, // 433A
				0, MTrip.HEADSIGN_TYPE_STRING, CLOVER_BAR, //
				1, MTrip.HEADSIGN_TYPE_STRING, ABJ) //
				.addALLFromTo(0, STOP_ID_7921, STOP_ID_8114) //
				.addALLFromTo(1, STOP_ID_8113, STOP_ID_7920) //
		);
		map.put(440l, new RouteTripSpec(440l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, HERITAGE_HILLS) //
				.addALLFromTo(0, STOP_ID_7199, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_7199) //
		);
		map.put(441l, new RouteTripSpec(441l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, TC) //
				.addALLFromTo(0, STOP_ID_4000, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_4000) //
		);
		map.put(442l, new RouteTripSpec(442l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, NOTTINGHAM) //
				.addALLFromTo(0, STOP_ID_9015, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_9015) //
		);
		map.put(443l, new RouteTripSpec(443l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, TC) //
				.addALLFromTo(0, STOP_ID_4000, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_4000) //
		);
		map.put(443l + RID_EW_B, new RouteTripSpec(443l + RID_EW_B, // 443B
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, OAK_ST) //
				.addALLFromTo(0, STOP_ID_6048, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_6029) //
		);
		map.put(450l, new RouteTripSpec(450l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, CITP) //
				.addALLFromTo(0, STOP_ID_2001, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_2001) //
		);
		map.put(451l, new RouteTripSpec(451l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, TC) //
				.addALLFromTo(0, STOP_ID_4000, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_4000) //
		);
		map.put(451l + RID_EW_A, new RouteTripSpec(451l + RID_EW_A, // 451A
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, VILLAGE) //
				.addALLFromTo(0, STOP_ID_5040, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_5115) //
		);
		map.put(451l + RID_EW_B, new RouteTripSpec(451l + RID_EW_B, // 451B
				0, MTrip.HEADSIGN_TYPE_STRING, BTT, //
				1, MTrip.HEADSIGN_TYPE_STRING, BROADMOOR) //
				.addALLFromTo(0, STOP_ID_5041, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_5041) //
		);
		ALL_ROUTE_TRIPS = map;
	}

	@Override
	public HashSet<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS.containsKey(mRoute.id)) {
			return ALL_ROUTE_TRIPS.get(mRoute.id).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, HashSet<MTrip> splitTrips, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS.containsKey(mRoute.id)) {
			RouteTripSpec rts = ALL_ROUTE_TRIPS.get(mRoute.id);
			return splitTripStop(gTrip, gTripStop, gtfs, //
					rts.getBeforeAfterStopIds(0), //
					rts.getBeforeAfterStopIds(1), //
					rts.getBeforeAfterBothStopIds(0), //
					rts.getBeforeAfterBothStopIds(1), //
					rts.getTripId(0), //
					rts.getTripId(1), //
					rts.getAllBeforeAfterStopIds());
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, gtfs);
	}

	private Pair<Long[], Integer[]> splitTripStop(GTrip gTrip, GTripStop gTripStop, GSpec gtfs, List<String> stopIdsTowards1, List<String> stopIdsTowards2,
			List<String> stopIdsTowardsBoth21, List<String> stopIdsTowardsBoth12, long tidTowardsStop1, long tidTowardsStop2, List<String> allBeforeAfterStopIds) {
		String beforeAfter = getBeforeAfterStopId(gtfs, gTrip, gTripStop, stopIdsTowards1, stopIdsTowards2, stopIdsTowardsBoth21, stopIdsTowardsBoth12,
				allBeforeAfterStopIds);
		if (stopIdsTowards1.contains(beforeAfter)) {
			return new Pair<Long[], Integer[]>(new Long[] { tidTowardsStop1 }, new Integer[] { gTripStop.getStopSequence() });
		} else if (stopIdsTowards2.contains(beforeAfter)) {
			return new Pair<Long[], Integer[]>(new Long[] { tidTowardsStop2 }, new Integer[] { gTripStop.getStopSequence() });
		} else if (stopIdsTowardsBoth21.contains(beforeAfter)) {
			return new Pair<Long[], Integer[]>(new Long[] { tidTowardsStop2, tidTowardsStop1 }, new Integer[] { 1, gTripStop.getStopSequence() });
		} else if (stopIdsTowardsBoth12.contains(beforeAfter)) {
			return new Pair<Long[], Integer[]>(new Long[] { tidTowardsStop1, tidTowardsStop2 }, new Integer[] { 1, gTripStop.getStopSequence() });
		}
		System.out.println("Unexptected trip stop to split " + gTripStop);
		System.exit(-1);
		return null;
	}

	private String getBeforeAfterStopId(GSpec gtfs, GTrip gTrip, GTripStop gTripStop, List<String> stopIdsTowards1, List<String> stopIdsTowards2,
			List<String> stopIdsTowardsBoth21, List<String> stopIdsTowardsBoth12, List<String> allBeforeAfterStopIds) {
		int gStopMaxSequence = -1;
		ArrayList<String> afterStopIds = new ArrayList<String>();
		ArrayList<Integer> afterStopSequence = new ArrayList<Integer>();
		ArrayList<String> beforeStopIds = new ArrayList<String>();
		ArrayList<Integer> beforeStopSequence = new ArrayList<Integer>();
		for (GStopTime gStopTime : gtfs.getStopTimes(gTrip.getTripId(), null, null)) {
			if (!gStopTime.trip_id.equals(gTrip.getTripId())) {
				continue;
			}
			if (allBeforeAfterStopIds.contains(gStopTime.getStopId())) {
				if (gStopTime.getStopSequence() < gTripStop.getStopSequence()) {
					beforeStopIds.add(gStopTime.getStopId());
					beforeStopSequence.add(gStopTime.getStopSequence());
				}
				if (gStopTime.getStopSequence() > gTripStop.getStopSequence()) {
					afterStopIds.add(gStopTime.getStopId());
					afterStopSequence.add(gStopTime.getStopSequence());
				}
			}
			if (gStopTime.getStopSequence() > gStopMaxSequence) {
				gStopMaxSequence = gStopTime.getStopSequence();
			}
		}
		if (allBeforeAfterStopIds.contains(gTripStop.getStopId())) {
			if (gTripStop.getStopSequence() == 1) {
				beforeStopIds.add(gTripStop.getStopId());
				beforeStopSequence.add(gTripStop.getStopSequence());
			}
			// System.out.println("max sequence: " + gStopMaxSequence);
			if (gTripStop.getStopSequence() == gStopMaxSequence) {
				afterStopIds.add(gTripStop.getStopId());
				afterStopSequence.add(gTripStop.getStopSequence());
			}
		}
		String beforeAfterStopIdCandidate = findBeforeAfterStopIdCandidate(gTripStop, stopIdsTowards1, stopIdsTowards2, stopIdsTowardsBoth21,
				stopIdsTowardsBoth12, afterStopIds, afterStopSequence, beforeStopIds, beforeStopSequence);
		if (beforeAfterStopIdCandidate != null) {
			return beforeAfterStopIdCandidate;
		}
		System.out.println("Unexpected trip (befores:" + beforeStopIds + "|afters:" + afterStopIds + ") " + gTrip);
		System.exit(-1);
		return null;
	}

	private String findBeforeAfterStopIdCandidate(GTripStop gTripStop, List<String> stopIdsTowards1, List<String> stopIdsTowards2,
			List<String> stopIdsTowardsBoth21, List<String> stopIdsTowardsBoth12, ArrayList<String> afterStopIds, ArrayList<Integer> afterStopSequence,
			ArrayList<String> beforeStopIds, ArrayList<Integer> beforeStopSequence) {
		String beforeAfterStopIdCurrent;
		Pair<Integer, String> beforeAfterStopIdCandidate = null;
		String beforeStopId, afterStopId;
		for (int b = 0; b < beforeStopIds.size(); b++) {
			beforeStopId = beforeStopIds.get(b);
			for (int a = 0; a < afterStopIds.size(); a++) {
				afterStopId = afterStopIds.get(a);
				beforeAfterStopIdCurrent = beforeStopId + DASH + afterStopId;
				if (stopIdsTowards1.contains(beforeAfterStopIdCurrent) || stopIdsTowards2.contains(beforeAfterStopIdCurrent)) {
					int size = Math.max(afterStopSequence.get(a) - gTripStop.getStopSequence(), gTripStop.getStopSequence() - beforeStopSequence.get(b));
					if (beforeAfterStopIdCandidate == null || size < beforeAfterStopIdCandidate.first) {
						beforeAfterStopIdCandidate = new Pair<Integer, String>(size, beforeAfterStopIdCurrent);
					}
				}
			}
		}
		for (int b = 0; b < beforeStopIds.size(); b++) {
			beforeStopId = beforeStopIds.get(b);
			beforeAfterStopIdCurrent = beforeStopId + DASH + ALL;
			if (stopIdsTowards1.contains(beforeAfterStopIdCurrent) || stopIdsTowards2.contains(beforeAfterStopIdCurrent)) {
				int size = gTripStop.getStopSequence() - beforeStopSequence.get(b);
				if (beforeAfterStopIdCandidate == null || size < beforeAfterStopIdCandidate.first) {
					beforeAfterStopIdCandidate = new Pair<Integer, String>(size, beforeAfterStopIdCurrent);
				}
			}
		}
		for (int a = 0; a < afterStopIds.size(); a++) {
			afterStopId = afterStopIds.get(a);
			beforeAfterStopIdCurrent = ALL + DASH + afterStopId;
			if (stopIdsTowards1.contains(beforeAfterStopIdCurrent) || stopIdsTowards2.contains(beforeAfterStopIdCurrent)) {
				int size = afterStopSequence.get(a) - gTripStop.getStopSequence();
				if (beforeAfterStopIdCandidate == null || size < beforeAfterStopIdCandidate.first) {
					beforeAfterStopIdCandidate = new Pair<Integer, String>(size, beforeAfterStopIdCurrent);
				}
			}
		}
		for (int b = 0; b < beforeStopIds.size(); b++) {
			beforeStopId = beforeStopIds.get(b);
			for (int a = 0; a < afterStopIds.size(); a++) {
				afterStopId = afterStopIds.get(a);
				if (gTripStop.getStopId().equals(beforeStopId) && gTripStop.getStopId().equals(afterStopId)) {
					continue;
				}
				beforeAfterStopIdCurrent = beforeStopId + DASH + afterStopId;
				if (stopIdsTowardsBoth21.contains(beforeAfterStopIdCurrent) || stopIdsTowardsBoth12.contains(beforeAfterStopIdCurrent)) {
					int size = Math.max(afterStopSequence.get(a) - gTripStop.getStopSequence(), gTripStop.getStopSequence() - beforeStopSequence.get(b));
					if (beforeAfterStopIdCandidate == null || size < beforeAfterStopIdCandidate.first) {
						beforeAfterStopIdCandidate = new Pair<Integer, String>(size, beforeAfterStopIdCurrent);
					}
				}
			}
		}
		for (int b = 0; b < beforeStopIds.size(); b++) {
			beforeStopId = beforeStopIds.get(b);
			beforeAfterStopIdCurrent = beforeStopId + DASH + ALL;
			if (stopIdsTowardsBoth21.contains(beforeAfterStopIdCurrent) || stopIdsTowardsBoth12.contains(beforeAfterStopIdCurrent)) {
				int size = gTripStop.getStopSequence() - beforeStopSequence.get(b);
				if (beforeAfterStopIdCandidate == null || size < beforeAfterStopIdCandidate.first) {
					beforeAfterStopIdCandidate = new Pair<Integer, String>(size, beforeAfterStopIdCurrent);
				}
			}
		}
		for (int a = 0; a < afterStopIds.size(); a++) {
			afterStopId = afterStopIds.get(a);
			beforeAfterStopIdCurrent = ALL + DASH + afterStopId;
			if (stopIdsTowardsBoth21.contains(beforeAfterStopIdCurrent) || stopIdsTowardsBoth12.contains(beforeAfterStopIdCurrent)) {
				int size = afterStopSequence.get(a) - gTripStop.getStopSequence();
				if (beforeAfterStopIdCandidate == null || size < beforeAfterStopIdCandidate.first) {
					beforeAfterStopIdCandidate = new Pair<Integer, String>(size, beforeAfterStopIdCurrent);
				}
			}
		}
		return beforeAfterStopIdCandidate.second;
	}

	private static final Pattern AND = Pattern.compile("( and )", Pattern.CASE_INSENSITIVE);
	private static final String AND_REPLACEMENT = " & ";

	private static final Pattern AT = Pattern.compile("( at )", Pattern.CASE_INSENSITIVE);
	private static final String AT_REPLACEMENT = " / ";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = AT.matcher(gStopName).replaceAll(AT_REPLACEMENT);
		gStopName = AND.matcher(gStopName).replaceAll(AND_REPLACEMENT);
		gStopName = MSpec.cleanStreetTypes(gStopName);
		gStopName = MSpec.cleanNumbers(gStopName);
		return MSpec.cleanLabel(gStopName);
	}

	private static class RouteTripSpec {

		private long routeId;
		private int directionId0;
		private int headsignType0;
		private String headsignString0;
		private int directionId1;
		private int headsignType1;
		private String headsignString1;

		public RouteTripSpec(long routeId, int directionId0, int headsignType0, String headsignString0, int directionId1, int headsignType1,
				String headsignString1) {
			this.routeId = routeId;
			this.directionId0 = directionId0;
			this.headsignType0 = headsignType0;
			this.headsignString0 = headsignString0;
			this.directionId1 = directionId1;
			this.headsignType1 = headsignType1;
			this.headsignString1 = headsignString1;
		}

		private ArrayList<String> allBeforeAfterStopIds = new ArrayList<String>();

		public ArrayList<String> getAllBeforeAfterStopIds() {
			return this.allBeforeAfterStopIds;
		}

		public long getTripId(int directionId) {
			return MTrip.getNewId(this.routeId, directionId);
		}

		private HashMap<Integer, ArrayList<String>> beforeAfterStopIds = new HashMap<Integer, ArrayList<String>>();

		public ArrayList<String> getBeforeAfterStopIds(int directionId) {
			if (!this.beforeAfterStopIds.containsKey(directionId)) {
				this.beforeAfterStopIds.put(directionId, new ArrayList<String>());
			}
			return this.beforeAfterStopIds.get(directionId);
		}

		private HashMap<Integer, ArrayList<String>> beforeAfterBothStopIds = new HashMap<Integer, ArrayList<String>>();

		public ArrayList<String> getBeforeAfterBothStopIds(int directionId) {
			if (!this.beforeAfterBothStopIds.containsKey(directionId)) {
				this.beforeAfterBothStopIds.put(directionId, new ArrayList<String>());
			}
			return this.beforeAfterBothStopIds.get(directionId);
		}

		private HashSet<MTrip> allTrips = null;

		public HashSet<MTrip> getAllTrips() {
			if (this.allTrips == null) {
				initAllTrips();
			}
			return this.allTrips;
		}

		private void initAllTrips() {
			this.allTrips = new HashSet<MTrip>();
			if (this.headsignType0 == MTrip.HEADSIGN_TYPE_STRING) {
				this.allTrips.add(new MTrip(this.routeId).setHeadsignString(this.headsignString0, this.directionId0));
			} else {
				System.out.println("Unexpected trip type " + this.headsignType0 + " for " + this.routeId);
				System.exit(-1);
			}
			if (this.headsignType1 == MTrip.HEADSIGN_TYPE_STRING) {
				this.allTrips.add(new MTrip(this.routeId).setHeadsignString(this.headsignString1, this.directionId1));
			} else {
				System.out.println("Unexpected trip type " + this.headsignType1 + " for " + this.routeId);
				System.exit(-1);
			}
		}

		public RouteTripSpec addALLFromTo(int directionId, String stopIdFrom, String stopIdTo) {
			addBeforeAfter(directionId, stopIdFrom + DASH + ALL);
			addBeforeAfter(directionId, ALL + DASH + stopIdTo);
			addBeforeAfter(directionId, stopIdFrom + DASH + stopIdTo);
			this.allBeforeAfterStopIds.add(stopIdFrom);
			this.allBeforeAfterStopIds.add(stopIdTo);
			return this;
		}

		@SuppressWarnings("unused")
		public RouteTripSpec addAllFrom(int directionId, String stopIdFrom) {
			addBeforeAfter(directionId, stopIdFrom + DASH + ALL);
			this.allBeforeAfterStopIds.add(stopIdFrom);
			return this;
		}

		@SuppressWarnings("unused")
		public RouteTripSpec addAllTo(int directionId, String stopIdTo) {
			addBeforeAfter(directionId, ALL + DASH + stopIdTo);
			this.allBeforeAfterStopIds.add(stopIdTo);
			return this;
		}

		@SuppressWarnings("unused")
		public RouteTripSpec addFromTo(int directionId, String stopIdFrom, String stopIdTo) {
			addBeforeAfter(directionId, stopIdFrom + DASH + stopIdTo);
			this.allBeforeAfterStopIds.add(stopIdFrom);
			this.allBeforeAfterStopIds.add(stopIdTo);
			return this;
		}

		private void addBeforeAfter(int directionId, String beforeAfterStopId) {
			if (!this.beforeAfterStopIds.containsKey(directionId)) {
				this.beforeAfterStopIds.put(directionId, new ArrayList<String>());
			}
			this.beforeAfterStopIds.get(directionId).add(beforeAfterStopId);
		}

		@SuppressWarnings("unused")
		public RouteTripSpec addAllBothFrom(int directionId, String stopIdFrom) {
			addBeforeAfterBoth(directionId, stopIdFrom + DASH + ALL);
			this.allBeforeAfterStopIds.add(stopIdFrom);
			return this;
		}

		@SuppressWarnings("unused")
		public RouteTripSpec addAllBothTo(int directionId, String stopIdTo) {
			addBeforeAfterBoth(directionId, ALL + DASH + stopIdTo);
			this.allBeforeAfterStopIds.add(stopIdTo);
			return this;
		}

		@SuppressWarnings("unused")
		public RouteTripSpec addBothFromTo(int directionId, String stopIdFrom, String stopIdTo) {
			addBeforeAfterBoth(directionId, stopIdFrom + DASH + stopIdTo);
			this.allBeforeAfterStopIds.add(stopIdFrom);
			this.allBeforeAfterStopIds.add(stopIdTo);
			return this;
		}

		private void addBeforeAfterBoth(int directionId, String beforeAfterStopId) {
			if (!this.beforeAfterBothStopIds.containsKey(directionId)) {
				this.beforeAfterBothStopIds.put(directionId, new ArrayList<String>());
			}
			this.beforeAfterBothStopIds.get(directionId).add(beforeAfterStopId);
		}
	}
}
