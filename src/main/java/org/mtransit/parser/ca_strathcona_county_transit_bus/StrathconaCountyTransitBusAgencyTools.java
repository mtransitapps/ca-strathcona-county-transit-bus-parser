package org.mtransit.parser.ca_strathcona_county_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.StringUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GIDs;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// https://data.strathcona.ca/
// https://data.strathcona.ca/Transportation/Transit-Bus-Schedule-GTFS-Data-Feed-Zip-File/2ek5-rxs5
// https://gtfs.edmonton.ca/TMGTFSRealTimeWebService/GTFS/GTFS.zip
// https://stalbert.ca/site/assets/files/3840/google_transit.zip
public class StrathconaCountyTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-strathcona-county-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new StrathconaCountyTransitBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Strathcona County Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Strathcona County Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final int AGENCY_ID_INT = GIDs.getInt("4"); // Strathcona County Transit

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		if (gRoute.isDifferentAgency(AGENCY_ID_INT)) {
			return true; // EXCLUDE
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		final String tripHeadSignLC = gTrip.getTripHeadsignOrDefault().toLowerCase(Locale.ENGLISH);
		if (tripHeadSignLC.contains("not in service")) {
			return true; // exclude
		}
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String A = "A";
	private static final String B = "B";

	private static final long RID_EW_A = 10_000L;
	private static final long RID_EW_B = 20_000L;

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName());
		}
		Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
		if (matcher.find()) {
			long id = Long.parseLong(matcher.group());
			if (gRoute.getRouteShortName().endsWith(A)) {
				return RID_EW_A + id;
			} else if (gRoute.getRouteShortName().endsWith(B)) {
				return RID_EW_B + id;
			}
		}
		throw new MTLog.Fatal("Unexpected route ID %s!", gRoute);
	}

	private static final String SLASH = " / ";
	private static final String BETHEL = "Bethel";
	private static final String BETHEL_TT = BETHEL + " TT";
	private static final String ORDZE_TC = "Ordze TC";
	private static final String DOWNTOWN = "Downtown";
	private static final String DAB = "Dial-A-Bus";
	private static final String EDM_CITY_CTR = "Edm City Ctr";
	private static final String GOV_CTR = "Gov Ctr";
	private static final String NAIT = "NAIT";
	private static final String GOV_CTR_NAIT = GOV_CTR + SLASH + NAIT;
	private static final String U_OF_ALBERTA = "U of Alberta";
	private static final String MILLENNIUM_PLACE = "Millennium Pl";
	private static final String EMERALD_HILLS = "Emerald Hls";
	private static final String ABJ = "ABJ Sch";
	private static final String EMERALD_HILLS_ABJ = EMERALD_HILLS + SLASH + ABJ;
	private static final String SUMMERWOOD = "Summerwood";
	private static final String CLARKDALE = "Clarkdale";
	private static final String HERITAGE_HILLS = "Heritage Hls";
	private static final String NOTTINGHAM = "Nottingham";
	private static final String BRENTWOOD = "Brentwood";
	private static final String GLEN_ALLAN = "Glen Allan";
	private static final String CITP = "Ctr in the Park";
	private static final String REGENCY = "Regency";

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
	private static final String RLN_441A = BETHEL_TT + " - " + REGENCY;
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

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteLongName())) {
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
					throw new MTLog.Fatal("Unexpected route long name %s!", gRoute);
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
				throw new MTLog.Fatal("Unexpected route long name %s!", gRoute);
			}
		}
		return cleanRouteLongName(gRoute);
	}

	private String cleanRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongNameOrDefault();
		routeLongName = routeLongName.toLowerCase(Locale.ENGLISH);
		routeLongName = CleanUtils.cleanSlashes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_GREEN = "559820"; // GREEN (from map PDF)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
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

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		String routeColor = gRoute.getRouteColor();
		if ("FFFFFF".equalsIgnoreCase(routeColor)) {
			routeColor = null;
		}
		if (StringUtils.isEmpty(routeColor)) {
			if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
				// @formatter:off
				if (RSN_441A.equals(gRoute.getRouteShortName())) { return "832B30";
				} else if (RSN_433A.equals(gRoute.getRouteShortName())) { return "ED0E58";
				} else if (RSN_443A.equals(gRoute.getRouteShortName())) { return "231F20";
				} else if (RSN_443B.equals(gRoute.getRouteShortName())) { return "00A34E";
				} else if (RSN_451A.equals(gRoute.getRouteShortName())) { return "6E6EAB";
				} else if (RSN_451B.equals(gRoute.getRouteShortName())) { return "D04CAE";
				// @formatter:on
				} else {
					throw new MTLog.Fatal("Unexpected route color %s!", gRoute);
				}
			}
			int rsn = Integer.parseInt(gRoute.getRouteShortName());
			switch (rsn) {
			// @formatter:off
			case 401: return "F78F20";
			case 403: return "9F237E";
			case 404: return "FFC745";
			case 411: return "6BC7B9";
			case 413: return "0076BC";
			case 414: return "F16278";
			case 420: return "ED1C24";
			case 430: return "2E3192";
			case 431: return "FFF30C";
			case 432: return "08796F";
			case 433: return "652290";
			case 440: return "7BC928";
			case 441: return "832B30";
			case 442: return "2E3192";
			case 443: return "006A2F";
			case 450: return "EC008C";
			case 451: return "F57415";
			case 490: return "1270BB";
			case 491: return "ED2D32";
			case 492: return "0F6B3B";
			case 493: return "61CACA";
			case 494: return "E59A12";
			case 495: return null;
			// @formatter:on
			default:
				throw new MTLog.Fatal("Unexpected route color %s!", gRoute);
			}
		}
		return super.getRouteColor(gRoute);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean directionFinderEnabled(long routeId, @NotNull GRoute gRoute) {
		int rID = (int) routeId;
		switch (rID) {
		case 401:
		case 403:
		case 413:
		case 420:
		case 430:
		case 432:
		case 433:
		case 440:
		case (int) (441L + RID_EW_A): // 441A
		case 442:
		case 450:
		case 490:
		case 491:
		case 492:
		case 493:
		case 494:
			return false; // DISABLED because useless direction_id (1 direction_id for 2 directions)
		}
		return super.directionFinderEnabled(routeId, gRoute);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("Unexpected trips to merge: %s & %s!", mTrip, mTripToMerge);
	}

	private static final Pattern EXPRESS_ = CleanUtils.cleanWords("express");

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = EXPRESS_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.cleanBounds(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		//noinspection deprecation
		map2.put(401L, new RouteTripSpec(401L, // BECAUSE 2 directions with same direction ID
				0, MTrip.HEADSIGN_TYPE_STRING, ORDZE_TC, //
				1, MTrip.HEADSIGN_TYPE_STRING, DOWNTOWN) //
				.addTripSort(0, //
						Arrays.asList( //
								"1973", // 107 St & 104 Av
								"1292", // ++
								"4000" // Ordze Transit Centre
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"4000", // Ordze Transit Centre
								"1457", // ++
								"1973" // 107 St & 104 Av
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(404L, new RouteTripSpec(404L, // BECAUSE 2 directions with same direction ID
				0, MTrip.HEADSIGN_TYPE_STRING, ORDZE_TC, //
				1, MTrip.HEADSIGN_TYPE_STRING, U_OF_ALBERTA) //
				.addTripSort(0, //
						Arrays.asList( //
								"2636", // University Transit Centre
								"2722", // ++
								"4000" // Ordze Transit Centre
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"4000", // Ordze Transit Centre
								"2752", // ++
								"2638", // 114 St & 85 Av
								"2625", // ++
								"2636" // University Transit Centre
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(413L, new RouteTripSpec(413L, // BECAUSE 2 directions with same direction ID
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, GOV_CTR_NAIT) //
				.addTripSort(0, //
						Arrays.asList( //
								"1223", // NAIT 106 St & 117 Av
								"1973", // <> GOV 107 St & 104 Av
								"1732", // GOV 107 St & 103 Av
								"1643", // ++
								"1629", // ++
								"8005", // ++
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // Bethel Transit Terminal
								"8004", // ++
								"1728", // ++
								"1898", // ++
								"1973", // <> GOV 107 St & 104 Av
								"1227" // NAIT 106 St & 117 Av
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(420L, new RouteTripSpec(420L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, MILLENNIUM_PLACE) //
				.addTripSort(0, //
						Arrays.asList( //
								"8800", // Premier Wy & Millennium Place
								"8811", // Strathmoor Dr
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // Bethel Transit Terminal
								"8700", // ++
								"8800" // Premier Wy & Millennium Place
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(430L, new RouteTripSpec(430L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, EMERALD_HILLS_ABJ) //
				.addTripSort(0, //
						Arrays.asList( //
								"7921", // Emerald Dr & ABJ
								"7436", // ++
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // Bethel Transit Terminal
								"8304", // ++
								"7921" // Emerald Dr & ABJ
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(431L, new RouteTripSpec(431L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, EMERALD_HILLS) // EMERALD_HILLS_ABJ
				.addTripSort(0, //
						Arrays.asList( //
								"7920", // Emerald Dr & ABJ
								"8849", // ++
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // Bethel Transit Terminal
								"7437", // Dawson Dr & Donnely Terr
								"7920" // Emerald Dr & ABJ
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(432L, new RouteTripSpec(432L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, SUMMERWOOD) //
				.addTripSort(0, //
						Arrays.asList( //
								"7870", // Lakeland Dr & Aspen Tr
								"7508", // ++
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // Bethel Transit Terminal
								"8304", // ++
								"7870" // Lakeland Dr & Aspen Tr
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(433L, new RouteTripSpec(433L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, "Davidson Crk") // SUMMERWOOD
				.addTripSort(0, //
						Arrays.asList( //
								"7272", // Primrose Blvd & Clover Bar Rd
								"7431", // Davidson Dr & Darlington Dr
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // Bethel Transit Terminal
								"8135", // ++
								"7272" // Primrose Blvd & Clover Bar Rd
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(440L, new RouteTripSpec(440L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, HERITAGE_HILLS) //
				.addTripSort(0, //
						Arrays.asList( //
								"8000", // != xx Bethel Transit Terminal #WTF!
								"7199", // Highland Wy & Heritage Dr
								"7124", // == Highland Dr & Heritage Lake Wy
								"7011", // !=
								"1068", // !=
								"7102", // ==
								"8000" // xx Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // == Bethel Transit Terminal
								"1090", // !=
								"7024", // !=
								"7199" // == Highland Wy & Heritage Dr
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(441L + RID_EW_A, new RouteTripSpec(441L + RID_EW_A, // 441A
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, REGENCY) //
				.addTripSort(0, //
						Arrays.asList( //
								"9115", // != Ritchie Wy & Rainbow Cr <=
								"9157", // != Ritchie Wy & Regency Dr <=
								"9239", // != Foxhaven Dr & Foxhaven Ct
								"9160", // == Clover Bar Rd & Foxhaven Dr
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // Bethel Transit Terminal
								"9150", // == Foxhaven Dr & Foxhaven Pl
								"9240", // != Foxhaven Dr & Foxhaven Pl
								"9115", // != Ritchie Wy & Rainbow Cr =>
								"9157" // != Ritchie Wy & Regency Dr =>
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(442L, new RouteTripSpec(442L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, NOTTINGHAM) //
				.addTripSort(0, //
						Arrays.asList( //
								"9015", // Nottingham Blvd & Nottingham Rd
								"9180", // Granada Blvd & Forrest Dr
								"8000"// Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // Bethel Transit Terminal
								"1002", // ++
								"9015" // Nottingham Blvd & Nottingham Rd
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(450L, new RouteTripSpec(450L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, CITP) //
				.addTripSort(0, //
						Arrays.asList( //
								"2001", // Festival Ln & Festival Av
								"6072", // ++
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // Bethel Transit Terminal
								"8105", // ++
								"2001" // Festival Ln & Festival Av
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(490L, new RouteTripSpec(490L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, MILLENNIUM_PLACE) //
				.addTripSort(0, //
						Arrays.asList( //
								"8800", // == Premier Wy & Millennium Place
								"8806", // Premier Wy & Prairie Dr
								"8812", // Strathmoor Dr
								"8814", // Streambank Av
								"8803", // Prairie Dr & Premier Wy
								"7526", // Summerland Dr & Lakeland Dr
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // Bethel Transit Terminal
								"8816", // != Streambank Av =>
								"8010", // != Broadway Blvd at Robin Hood CONTINUE
								"8700", // ++
								"8800" // == Premier Wy & Millennium Place
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(491L, new RouteTripSpec(491L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, SUMMERWOOD) //
				.addTripSort(0, //
						Arrays.asList( //
								"7807", // Clover Bar Rd & Jubilee Dr
								"7508", // Summerwood Blvd & Clover Bar Rd
								"7406", // Davidson Dr & Darlington Dr
								"7312", // Meadowview Dr & Clarkdale Dr
								"7229", // Primrose Blvd & Clover Bar Rd
								"8138", // Summerwood Blvd & Clover Bar Rd
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // == Bethel Transit Terminal
								"8010", // != Broadway Blvd at Robin Hood
								"8816", // != Streambank Av =>
								"8101", // != Bethel Dr & Broadview Rd
								"7604", // Jim Common Dr & Cache Pl
								"7807" // Clover Bar Rd & Jubilee Dr
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(492L, new RouteTripSpec(492L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, REGENCY) //
				.addTripSort(0, //
						Arrays.asList( //
								"9157", // Ritchie Wy & Regency Dr
								"9225", // Regency Dr & Ridgeland Cr
								"7199", // Highland Wy & Heritage Dr
								"7011", // Craigavon Dr & Carmel Rd
								"1042", // Galloway Dr & Glengarry Cr
								"1052", // Georgian Wy
								"1001", // Sherwood Dr & Granada Blvd
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // == Bethel Transit Terminal
								"8010", // != Broadway Blvd at Robin Hood
								"8816", // != Streambank Av =>
								"8101", // != Broadway Blvd at Robin Hood
								"1002", // Sherwood Dr & Oak St
								"9015", // Nottingham Blvd & Nottingham Rd
								"9157" // Ritchie Wy & Regency Dr
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(493L, new RouteTripSpec(493L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, BRENTWOOD) //
				.addTripSort(0, //
						Arrays.asList( //
								"6097", // Alder Av & Alderwood Cr
								"6115", // Alder Av & Ivy Cr
								"6065", // Fir St & Willow St
								"6048", // Oak St & Sherwood Dr
								"1004", // Sherwood Dr & Oak St
								"1090", // Galloway Dr & Glenbrook Blvd
								"7006", // Glencoe Blvd & Courtenay By
								"8111", // Sherwood Dr & Cranford Wy
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // == Bethel Transit Terminal
								"8010", // != Broadway Blvd at Robin Hood
								"8816", // != Streambank Av =>
								"8101", // != Broadway Blvd at Robin Hood
								"7011", // Fir St & Willow St
								"1042", // Galloway Dr & Glengarry Cr
								"1071", // Georgian Wy & Glenbrook Blvd
								"2001", // Festival Ln & Festival Av
								"6079", // Raven Dr & Crane Rd
								"6091", // Heron Rd & Falcon Dr
								"6097" // Alder Av & Alderwood Cr
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(494L, new RouteTripSpec(494L, //
				0, MTrip.HEADSIGN_TYPE_STRING, BETHEL_TT, //
				1, MTrip.HEADSIGN_TYPE_STRING, ORDZE_TC) // Woodbridge
				.addTripSort(0, //
						Arrays.asList( //
								"4000", // Ordze Transit Centre
								"6012", // Fir St & Willow St
								"9353", // Clover Bar Rd & Wye Rd
								"9180", // Granada Blvd & Forrest Dr
								"1001", // Sherwood Dr & Granada Blvd
								"1004", // != Sherwood Dr & Oak St
								"5005", // <> Main Blvd & Mardale Cr
								"5023", // <> Main Blvd & Marion Dr
								"4036", // <> Broadmoor Blvd & Main Blvd
								"8013", // != Broadview Rd & Broadmoor Blvd
								"8000" // Bethel Transit Terminal
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"8000", // == Bethel Transit Terminal
								"8010", // != Broadway Blvd at Robin Hood
								"8816", // != Streambank Av =>
								"1013", // != Sherwood Dr & Baseline Rd
								"1014", // != Sherwood Dr & Main Blvd
								"5005", // <> Main Blvd & Mardale Cr
								"5011", // != Main Blvd & Millers Rd
								"5023", // <> Main Blvd & Marion Dr
								"4036", // <> Broadmoor Blvd & Main Blvd
								"5041", // != Kaska Rd Chippewa Rd
								"5065", // Broadmoor Blvd & Sioux Rd
								"5089", // Haythorne Rd & Haythorne Pl
								"5115", // Village Dr & Village Dr
								"4000" // Ordze Transit Centre
						)) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	private static final Pattern STARTS_WITH_S_ = Pattern.compile("((^)(S))", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanStopOriginalId(@NotNull String gStopId) {
		gStopId = STARTS_WITH_S_.matcher(gStopId).replaceAll(EMPTY);
		return gStopId;
	}

	@Override
	public int compareEarly(long routeId, @NotNull List<MTripStop> list1, @NotNull List<MTripStop> list2, @NotNull MTripStop ts1, @NotNull MTripStop ts2, @NotNull GStop ts1GStop, @NotNull GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@NotNull
	@Override
	public ArrayList<MTrip> splitTrip(@NotNull MRoute mRoute, @Nullable GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@NotNull
	@Override
	public Pair<Long[], Integer[]> splitTripStop(@NotNull MRoute mRoute, @NotNull GTrip gTrip, @NotNull GTripStop gTripStop, @NotNull ArrayList<MTrip> splitTrips, @NotNull GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	@Override
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		String stopId = cleanStopOriginalId(gStop.getStopId());
		if (!Utils.isDigitsOnly(stopId)) {
			Matcher matcher = DIGITS.matcher(stopId);
			if (matcher.find()) {
				int digits = Integer.parseInt(matcher.group());
				if (stopId.toLowerCase(Locale.ENGLISH).startsWith("s")) {
					return 1_900_000 + digits;
				}
			}
			throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop);
		}
		return Integer.parseInt(stopId);
	}

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		if ("0".equals(gStop.getStopCode())) {
			return EMPTY;
		}
		return super.getStopCode(gStop);
	}
}
