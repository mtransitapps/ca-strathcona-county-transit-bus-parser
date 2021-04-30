package org.mtransit.parser.ca_strathcona_county_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.ColorUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GAgency;
import org.mtransit.parser.gtfs.data.GIDs;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// https://data.strathcona.ca/
// https://data.strathcona.ca/Transportation/Transit-Bus-Schedule-GTFS-Data-Feed-Zip-File/2ek5-rxs5
// https://gtfs.edmonton.ca/TMGTFSRealTimeWebService/GTFS/GTFS.zip
public class StrathconaCountyTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new StrathconaCountyTransitBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Strathcona County Transit";
	}

	private static final int AGENCY_ID_INT = GIDs.getInt("4"); // Strathcona County Transit

	@Override
	public boolean excludeAgency(@NotNull GAgency gAgency) {
		if (gAgency.getAgencyIdInt() != AGENCY_ID_INT) {
			return EXCLUDE;
		}
		return super.excludeAgency(gAgency);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		final String tripHeadSignLC = gTrip.getTripHeadsignOrDefault().toLowerCase(Locale.ENGLISH);
		if (tripHeadSignLC.contains("not in service")) {
			return true; // exclude
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
		if (CharUtils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName());
		}
		final Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
		if (matcher.find()) {
			final long id = Long.parseLong(matcher.group());
			if (gRoute.getRouteShortName().endsWith(A)) {
				return RID_EW_A + id;
			} else if (gRoute.getRouteShortName().endsWith(B)) {
				return RID_EW_B + id;
			}
		}
		throw new MTLog.Fatal("Unexpected route ID %s!", gRoute);
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
		if (ColorUtils.WHITE.equalsIgnoreCase(routeColor)) {
			routeColor = null;
		}
		if (StringUtils.isEmpty(routeColor)) {
			if (!CharUtils.isDigitsOnly(gRoute.getRouteShortName())) {
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
	public boolean directionSplitterEnabled() {
		return true;
	}

	@Override
	public boolean directionSplitterEnabled(long routeId) {
		//noinspection RedundantIfStatement
		if (routeId == 413L
				|| routeId == 441L
				|| routeId == 443L
				|| routeId == 451L) {
			return true;
		}
		return false;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern EXPRESS_ = CleanUtils.cleanWords("express");

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = EXPRESS_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanBounds(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern STARTS_WITH_S_ = Pattern.compile("((^)(S))", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanStopOriginalId(@NotNull String gStopId) {
		gStopId = STARTS_WITH_S_.matcher(gStopId).replaceAll(EMPTY);
		return gStopId;
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
		final String stopId = cleanStopOriginalId(gStop.getStopId());
		if (!CharUtils.isDigitsOnly(stopId)) {
			final Matcher matcher = DIGITS.matcher(stopId);
			if (matcher.find()) {
				final int digits = Integer.parseInt(matcher.group());
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
