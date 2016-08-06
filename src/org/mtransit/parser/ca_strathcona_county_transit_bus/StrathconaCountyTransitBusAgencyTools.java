package org.mtransit.parser.ca_strathcona_county_transit_bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

// https://data.strathcona.ca/
// https://data.strathcona.ca/Transportation/Strathcona-County-Transit-Bus-Schedule-GTFS-Data-F/2ek5-rxs5
// https://data.strathcona.ca/download/2ek5-rxs5/ZIP
// http://webpub2.strathcona.ab.ca/GTFS/Google_Transit.zip
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
		System.out.printf("\nGenerating Strathcona County Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Strathcona County Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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
		if (Utils.isDigitsOnly(gRoute.getRouteId())) {
			return Long.parseLong(gRoute.getRouteId());
		}
		Matcher matcher = DIGITS.matcher(gRoute.getRouteId());
		matcher.find();
		long id = Long.parseLong(matcher.group());
		if (gRoute.getRouteId().endsWith(A)) {
			return RID_EW_A + id;
		} else if (gRoute.getRouteId().endsWith(B)) {
			return RID_EW_B + id;
		}
		System.out.printf("\nUnexpected route ID %s!\n", gRoute);
		System.exit(-1);
		return -1l;
	}

	private static final String BETHEL_TT = "Bethel TT";
	private static final String ORDZE_TC = "Ordze TC";
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

	private static final String RLN_401 = ORDZE_TC + " - " + EDM_CITY_CTR;
	private static final String RLN_403 = ORDZE_TC + " - " + GOV_CTR;
	private static final String RLN_404 = ORDZE_TC + " - " + U_OF_ALBERTA;
	private static final String RLN_411 = BETHEL_TT + " - " + EDM_CITY_CTR;
	private static final String RLN_413 = BETHEL_TT + " - " + GOV_CTR_NAIT;
	private static final String RLN_414 = BETHEL_TT + " - " + U_OF_ALBERTA;
	private static final String RLN_420 = BETHEL_TT + " - " + MILLENNIUM_PLACE;
	private static final String RLN_430 = BETHEL_TT + " - " + EMERALD_HILLS + " CW";
	private static final String RLN_431 = BETHEL_TT + " - " + EMERALD_HILLS + " CCW";
	private static final String RLN_432 = BETHEL_TT + " -" + SUMMERWOOD;
	private static final String RLN_433 = BETHEL_TT + " - " + CLARKDALE;
	private static final String RLN_441A = BETHEL_TT + " - " + "Regency";
	private static final String RLN_433A = CLARKDALE + " - " + ABJ;
	private static final String RLN_440 = BETHEL_TT + " - " + HERITAGE_HILLS;
	private static final String RLN_441 = BETHEL_TT + " - " + ORDZE_TC + " - Regency";
	private static final String RLN_442 = BETHEL_TT + " - " + NOTTINGHAM;
	private static final String RLN_443 = BETHEL_TT + " - " + ORDZE_TC + " - Glen Allan";
	private static final String RLN_443A = BETHEL_TT + " - " + BRENTWOOD;
	private static final String RLN_443B = BETHEL_TT + " - " + GLEN_ALLAN;
	private static final String RLN_450 = BETHEL_TT + " - " + CITP;
	private static final String RLN_451 = BETHEL_TT + " - " + ORDZE_TC + " - Mills Haven / Westboro";
	private static final String RLN_451A = BETHEL_TT + " - Woodbridge";
	private static final String RLN_451B = BETHEL_TT + " - Mills Haven";
	private static final String RLN_490 = DAB + " A";
	private static final String RLN_491 = DAB + " B";
	private static final String RLN_492 = DAB + " C";
	private static final String RLN_493 = DAB + " D";
	private static final String RLN_494 = DAB + " E";
	private static final String RLN_495 = DAB + " F";

	@Override
	public String getRouteLongName(GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			// @formatter:off
			if (RSN_441A.equals(gRoute.getRouteShortName())) { return RLN_441A;
			} else if (RSN_433A.equals(gRoute.getRouteShortName())) { return RLN_433A;
			} else if (RSN_443A.equals(gRoute.getRouteShortName())) { return RLN_443A;
			} else if (RSN_443B.equals(gRoute.getRouteShortName())) { return RLN_443B;
			} else if (RSN_451A.equals(gRoute.getRouteShortName())) { return RLN_451A;
			} else if (RSN_451B.equals(gRoute.getRouteShortName())) { return RLN_451B;
			// @formatter:on
			} else {
				System.out.printf("\nUnexpected route long name %s!\n", gRoute);
				System.exit(-1);
				return null;
			}
		}
		int rsn = Integer.parseInt(gRoute.getRouteShortName());
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
			System.out.printf("\nUnexpected route long name %s!\n", gRoute);
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

	private static final String RSN_441A = "441A";
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
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			// @formatter:off
			if (RSN_441A.equals(gRoute.getRouteShortName())) { return COLOR_832B30;
			} else if (RSN_433A.equals(gRoute.getRouteShortName())) { return COLOR_ED0E58;
			} else if (RSN_443A.equals(gRoute.getRouteShortName())) { return COLOR_231F20;
			} else if (RSN_443B.equals(gRoute.getRouteShortName())) { return COLOR_00A34E;
			} else if (RSN_451A.equals(gRoute.getRouteShortName())) { return COLOR_6E6EAB;
			} else if (RSN_451B.equals(gRoute.getRouteShortName())) { return COLOR_D04CAE;
			// @formatter:on
			} else {
				System.out.printf("\nUnexpected route color %s!\n", gRoute);
				System.exit(-1);
				return null;
			}
		}
		int rsn = Integer.parseInt(gRoute.getRouteShortName());
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
			System.out.printf("\nUnexpected route color %s!\n", gRoute);
			System.exit(-1);
			return null;
		}
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS.containsKey(mRoute.getId())) {
			return; // split
		}
		if (mRoute.getId() == 443l + RID_EW_A) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BETHEL_TT, 0);
				return;
			}
			System.out.printf("\n%s: Unexpected trip: %s\n", mRoute.getId(), gTrip);
			System.exit(-1);
		} else if (mRoute.getId() == 495l) {
			if (gTrip.getTripHeadsign().equals("FULL")) {
				mTrip.setHeadsignString("Sherwood Dr / Oak St", 0);
				return;
			} else if (gTrip.getTripHeadsign().equals("LAST")) {
				mTrip.setHeadsignString("Streambank Ave", 1);
				return;
			}
			System.out.printf("\n%s: Unexpected trip: %s\n", mRoute.getId(), gTrip);
			System.exit(-1);
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	private static final Pattern TRANSIT_TERMINAL = Pattern.compile("(transit terminal)", Pattern.CASE_INSENSITIVE);
	private static final String TRANSIT_TERMINAL_REPLACEMENT = "TT";
	private static final Pattern TRANSIT_CENTER = Pattern.compile("(transit (center|centre))", Pattern.CASE_INSENSITIVE);
	private static final String TRANSIT_CENTER_REPLACEMENT = "TC";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = TRANSIT_TERMINAL.matcher(tripHeadsign).replaceAll(TRANSIT_TERMINAL_REPLACEMENT);
		tripHeadsign = TRANSIT_CENTER.matcher(tripHeadsign).replaceAll(TRANSIT_CENTER_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

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
				0, MTrip.HEADSIGN_TYPE_STRING, ORDZE_TC, //
				1, MTrip.HEADSIGN_TYPE_STRING, EDM_CITY_CTR) //
				.addALLFromTo(0, STOP_ID_1832, STOP_ID_4000) //
				.addALLFromTo(1, STOP_ID_4000, STOP_ID_1832) //
		);
		map.put(403l, new RouteTripSpec(403l, //
				0, MTrip.HEADSIGN_TYPE_STRING, ORDZE_TC, //
				1, MTrip.HEADSIGN_TYPE_STRING, EDM_CITY_CTR) //
				.addALLFromTo(0, STOP_ID_1732, STOP_ID_4000) //
				.addALLFromTo(1, STOP_ID_4000, STOP_ID_1973) //
		);
		map.put(404l, new RouteTripSpec(404l, //
				0, MTrip.HEADSIGN_TYPE_STRING, ORDZE_TC, //
				1, MTrip.HEADSIGN_TYPE_STRING, U_OF_ALBERTA) //
				.addALLFromTo(0, STOP_ID_2636, STOP_ID_4000) //
				.addALLFromTo(1, STOP_ID_4000, STOP_ID_2636) //
		);
		map.put(411l, new RouteTripSpec(411l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, EDM_CITY_CTR) //
				.addALLFromTo(0, STOP_ID_1973, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_1973) //
		);
		map.put(413l, new RouteTripSpec(413l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, NAIT_GOV_CTR) //
				.addALLFromTo(0, STOP_ID_1223, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_1227) //
		);
		map.put(414l, new RouteTripSpec(414l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, U_OF_ALBERTA) //
				.addALLFromTo(0, STOP_ID_2636, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_2636) //
		);
		map.put(420l, new RouteTripSpec(420l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, STRATHMOOR_DRIVE) //
				.addALLFromTo(0, STOP_ID_8811, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_8810) //
		);
		map.put(430l, new RouteTripSpec(430l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, EMERALD_HILLS_ABJ) //
				.addALLFromTo(0, STOP_ID_7921, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_7921) //
		);
		map.put(431l, new RouteTripSpec(431l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, EMERALD_HILLS_ABJ) //
				.addALLFromTo(0, STOP_ID_7920, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_7920) //
		);
		map.put(432l, new RouteTripSpec(432l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, SUMMERWOOD) //
				.addALLFromTo(0, STOP_ID_7508, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_7508) //
		);
		map.put(433l, new RouteTripSpec(433l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
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
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, HERITAGE_HILLS) //
				.addALLFromTo(0, STOP_ID_7199, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_7199) //
		);
		map.put(441l, new RouteTripSpec(441l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, ORDZE_TC) //
				.addALLFromTo(0, STOP_ID_4000, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_4000) //
		);
		map.put(442l, new RouteTripSpec(442l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, NOTTINGHAM) //
				.addALLFromTo(0, STOP_ID_9015, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_9015) //
		);
		map.put(443l, new RouteTripSpec(443l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, ORDZE_TC) //
				.addALLFromTo(0, STOP_ID_4000, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_4000) //
		);
		map.put(443l + RID_EW_B, new RouteTripSpec(443l + RID_EW_B, // 443B
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, OAK_ST) //
				.addALLFromTo(0, STOP_ID_6048, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_6029) //
		);
		map.put(450l, new RouteTripSpec(450l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, CITP) //
				.addALLFromTo(0, STOP_ID_2001, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_2001) //
		);
		map.put(451l, new RouteTripSpec(451l, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, ORDZE_TC) //
				.addALLFromTo(0, STOP_ID_4000, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_4000) //
		);
		map.put(451l + RID_EW_A, new RouteTripSpec(451l + RID_EW_A, // 451A
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, VILLAGE) //
				.addALLFromTo(0, STOP_ID_5040, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_5115) //
		);
		map.put(451l + RID_EW_B, new RouteTripSpec(451l + RID_EW_B, // 451B
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, BROADMOOR) //
				.addALLFromTo(0, STOP_ID_5041, STOP_ID_8000) //
				.addALLFromTo(1, STOP_ID_8000, STOP_ID_5041) //
		);
		ALL_ROUTE_TRIPS = map;
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS.get(mRoute.getId()));
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}


	@Override
	public String cleanStopName(String gStopName) {
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public String getStopCode(GStop gStop) {
		if ("0".equals(gStop.getStopCode())) {
			return null;
		}
		return super.getStopCode(gStop);
	}
}
