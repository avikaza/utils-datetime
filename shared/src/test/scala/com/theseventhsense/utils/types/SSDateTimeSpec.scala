package com.theseventhsense.utils.types

import cats.data.Xor
import com.theseventhsense.utils.types.SSDateTime.{ DateTime, KnownTimeZone, TimeZone }
import com.theseventhsense.utils.types.SSDateTime.TimeZone.{ Europe, US, UTC }
import org.scalatest._

/**
 * Created by erik on 1/22/16.
 */
class SSDateTimeSpec extends WordSpec with MustMatchers with OptionValues {
  "the instant class" should {
    "be able to add days" in {
      SSDateTime.Instant(1000).plusDays(1).millis mustEqual 24 * 60 * 60 * 1000 + 1000
    }
    "be able to subtract days" in {
      SSDateTime.Instant(1000000).minusDays(1).millis mustEqual 1000000 - 24 * 60 * 60 * 1000
    }
    "be able to parse common instant formats" in {
      SSDateTime.Instant.parse("1424298423000") mustEqual Xor.right(SSDateTime.Instant(1424298423000L))
      SSDateTime.Instant.parse("2015-02-18T22:27:03Z") mustEqual Xor.right(SSDateTime.Instant(1424298423000L))
      SSDateTime.Instant.parse("2015-02-18T17:27:03-0500") mustEqual Xor.right(SSDateTime.Instant(1424298423000L))
      SSDateTime.Instant.parse(null) mustEqual Xor.right(SSDateTime.Instant(0L))
      //      SSDateTime.Instant.parse("2015-02-18T17:27:03 EST5EDT") mustEqual SSDateTime.Instant(1424298423000L)
    }
  }

  "the TimeZone class" should {
    "recognize known timezones by name" in {
      KnownTimeZone("America/New_York").value mustEqual US.Eastern
      KnownTimeZone("America/Chicago").value mustEqual US.Central
      KnownTimeZone("America/Denver").value mustEqual US.Mountain
      KnownTimeZone("America/Los_Angeles").value mustEqual US.Pacific
      KnownTimeZone("Europe/Bucharest").value mustEqual Europe.Eastern
      KnownTimeZone("Europe/London").value mustEqual Europe.Central
      KnownTimeZone("Europe/Berlin").value mustEqual Europe.Western
    }
    "parse known timezones by name" in {
      TimeZone.from("America/New_York") mustEqual US.Eastern
    }
    "be able to check zone name validity" in {
      TimeZone.all.map(tz => tz.valid)
    }
    "be able to parse unknown zones" in {
      SSDateTime.TimeZone.parse("Europe/Athens") mustBe a[Xor.Right[_]]
    }
    "be able to parse unknown zones with wierd offsets" in {
      SSDateTime.TimeZone.parse("100").toOption.value.name mustEqual SSDateTime.TimeZone
        .parse("+01:00")
        .toOption
        .value
        .name
    }
    "fail to parse invalid zones" in {
      SSDateTime.TimeZone.parse("Europ/Bogus") mustBe an[Xor.Left[_]]
    }
    "determine ZoneIds for TimeZones" ignore {
      //      TimeZone.all.map(_.asZoneId).size mustEqual TimeZone.all.size
      //      TimeZone.UTC.asZoneId mustEqual ZoneId.of("UTC")
    }
    "normalize known timezones by alias" in {
      KnownTimeZone.from("US/Eastern").value mustEqual US.Eastern
    }
    "normalize known timezones by offset" in {
      val parsed = SSDateTime.Instant.parse("2016-02-01T00:00:00Z")
      parsed mustBe an[Xor.Right[_]]
      val now = parsed.toOption.value
      KnownTimeZone.from("-05:00", now).value mustEqual US.Eastern
      KnownTimeZone.from("-0500", now).value mustEqual US.Eastern
      KnownTimeZone.from("-500", now).value mustEqual US.Eastern
      KnownTimeZone.from("+01:00", now).value mustEqual Europe.Western
      KnownTimeZone.from("+0100", now).value mustEqual Europe.Western
      KnownTimeZone.from("0100", now).value mustEqual Europe.Western
      KnownTimeZone.from("+100", now).value mustEqual Europe.Western
      KnownTimeZone.from("100", now).value mustEqual Europe.Western
      KnownTimeZone.from("0", now).value mustEqual UTC
      KnownTimeZone.from("00", now).value mustEqual UTC
      KnownTimeZone.from("000", now).value mustEqual UTC
      KnownTimeZone.from("0000", now).value mustEqual UTC
      KnownTimeZone.from("+0000", now).value mustEqual UTC
      KnownTimeZone.from("-0000", now).value mustEqual UTC
    }
    "normalize known timezones by offset during daylight savings time" in {
      val now = SSDateTime.Instant.parse("2016-06-01T00:00:00Z").toOption.value
      KnownTimeZone.from("-05:00", now).value mustEqual US.Central
      KnownTimeZone.from("-0500", now).value mustEqual US.Central
      KnownTimeZone.from("-500", now).value mustEqual US.Central
      KnownTimeZone.from("+01:00", now).value mustEqual Europe.Central
      KnownTimeZone.from("+0100", now).value mustEqual Europe.Central
      KnownTimeZone.from("0100", now).value mustEqual Europe.Central
      KnownTimeZone.from("+100", now).value mustEqual Europe.Central
      KnownTimeZone.from("100", now).value mustEqual Europe.Central
      KnownTimeZone.from("0", now).value mustEqual UTC
      KnownTimeZone.from("00", now).value mustEqual UTC
      KnownTimeZone.from("000", now).value mustEqual UTC
      KnownTimeZone.from("0000", now).value mustEqual UTC
      KnownTimeZone.from("+0000", now).value mustEqual UTC
      KnownTimeZone.from("-0000", now).value mustEqual UTC
    }
  }

  lazy val now = SSDateTime.DateTime.parse("2016-06-02T00:00:00Z")
  lazy val now2 = SSDateTime.DateTime.parse("2016-06-02T00:00:00Z")
  lazy val nowEastern = SSDateTime.DateTime(
    SSDateTime.Instant.parse("2016-06-01T19:00:00Z").toOption.value, SSDateTime.TimeZone.US.Eastern
  )
  lazy val nowCentral = SSDateTime.DateTime(
    SSDateTime.Instant.parse("2016-06-01T18:00:00Z").toOption.value, SSDateTime.TimeZone.US.Central
  )
  "the DateTime class" should {
    "parse dates" in {
      now mustBe an[Xor.Right[_]]
    }
    "recognize seperate objects which are equal" ignore {
      now.toOption.value.isEqual(now2.toOption.value) mustEqual true
    }
    "recognize objects of the same instant but in different zones as not equal" ignore {
      now.toOption.value.isEqual(nowEastern) mustEqual false
    }
    "be able to adjust zones" ignore {
      nowEastern.withZone(SSDateTime.TimeZone.US.Central) mustEqual nowCentral
    }
  }
  "the Instant class" should {
    "be able to compare" ignore {
      now.toOption.value.instant.isEqual(nowEastern.instant) mustEqual true
    }
  }
}