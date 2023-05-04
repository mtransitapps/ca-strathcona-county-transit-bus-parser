package org.mtransit.parser.ca_strathcona_county_transit_bus;

import static org.mtransit.commons.RegexUtils.DIGITS;
import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.ColorUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://data.strathcona.ca/
// https://data.strathcona.ca/Transportation/Transit-Bus-Schedule-GTFS-Data-Feed-Zip-File/2ek5-rxs5
// https://gtfs.edmonton.ca/TMGTFSRealTimeWebService/GTFS/GTFS.zip
public class StrathconaCountyTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new StrathconaCountyTransitBusAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_EN;
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

	@Nullable
	@Override
	public String getAgencyId() {
		return "4"; // Strathcona County Transit
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

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_GREEN = "559820"; // GREEN (from map PDF)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Nullable
	@Override
	public String fixColor(@Nullable String color) {
		if (ColorUtils.WHITE.equalsIgnoreCase(color)) {
			return null;
		}
		return super.fixColor(color);
	}

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Nullable
	@Override
	public String provideMissingRouteColor(@NotNull GRoute gRoute) {
		return switch (gRoute.getRouteShortName()) {
			// @formatter:off
			case "401" -> "F78F20";
			case "403" -> "9F237E";
			case "404" -> "FFC745";
			case "411" -> "6BC7B9";
			case "413" -> "0076BC";
			case "414" -> "F16278";
			case "420" -> "ED1C24";
			case "430" -> "2E3192";
			case "431" -> "FFF30C";
			case "432" -> "08796F";
			case "433" -> "652290";
			case "433A" -> "ED0E58";
			case "440" -> "7BC928";
			case "441" -> "832B30";
			case "441A" -> "832B30";
			case "442" -> "2E3192";
			case "443" -> "006A2F";
			case "443A" -> "231F20";
			case "443B" -> "00A34E";
			case "450" -> "EC008C";
			case "451" -> "F57415";
			case "451A" -> "6E6EAB";
			case "451B" -> "D04CAE";
			case "490" -> "1270BB";
			case "491" -> "ED2D32";
			case "492" -> "0F6B3B";
			case "493" -> "61CACA";
			case "494" -> "E59A12";
			case "495"-> null; // TODO
			// @formatter:on
			default -> throw new MTLog.Fatal("Unexpected route color %s!", gRoute);
		};
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@Override
	public boolean directionSplitterEnabled(long routeId) {
		if (routeId == 401L) {
			return true; // ENABLED
		} else if (routeId == 404L) {
			return true; // ENABLED
		} else if (routeId == 420L) {
			return true; // ENABLED
		} else if (routeId == 432L) {
			return true; // ENABLED
		} else if (routeId == 433L) {
			return true; // ENABLED
		} else if (routeId == 442L) {
			return true; // ENABLED
		} else if (routeId == 450L) {
			return true; // ENABLED
		}
		return super.directionSplitterEnabled(routeId);
	}

	@Override
	public boolean directionOverrideId(long routeId) {
		if (routeId == 401L) {
			return true;
		} else if (routeId == 404L) {
			return true;
		} else if (routeId == 420L) {
			return true;
		} else if (routeId == 432L) {
			return true;
		} else if (routeId == 433L) {
			return true;
		} else if (routeId == 442L) {
			return true;
		} else if (routeId == 450L) {
			return true;
		}
		return super.directionOverrideId(routeId);
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
