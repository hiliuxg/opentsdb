// This file is part of OpenTSDB.
// Copyright (C) 2015-2017  The OpenTSDB Authors.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 2.1 of the License, or (at your
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// General Public License for more details.  You should have received a copy
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.
package net.opentsdb.query.pojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import net.opentsdb.query.pojo.Downsampler;
import net.opentsdb.utils.JSON;

import org.junit.Test;

public class TestDownsampler {
  
  @Test(expected = IllegalArgumentException.class)
  public void validationErrorWhenIntervalIsNull() throws Exception {
    String json = "{\"aggregator\":\"sum\"}";
    Downsampler downsampler = JSON.parseToObject(json, Downsampler.class);
    downsampler.validate();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void validationErrorWhenIntervalIsEmpty() throws Exception {
    String json = "{\"interval\":\"\",\"aggregator\":\"sum\"}";
    Downsampler downsampler = JSON.parseToObject(json, Downsampler.class);
    downsampler.validate();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void validationErrorWhenIntervalIsInvalid() throws Exception {
    String json = "{\"interval\":\"45foo\",\"aggregator\":\"sum\"}";
    Downsampler downsampler = JSON.parseToObject(json, Downsampler.class);
    downsampler.validate();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void validationErrorWhenAggregatorIsNull() throws Exception {
    String json = "{\"interval\":\"1h\"}";
    Downsampler downsampler = JSON.parseToObject(json, Downsampler.class);
    downsampler.validate();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void validationErrorWhenAggregatorIsEmpty() throws Exception {
    String json = "{\"interval\":\"1h\",\"aggregator\":\"\"}";
    Downsampler downsampler = JSON.parseToObject(json, Downsampler.class);
    downsampler.validate();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void validationErrorWhenAggregatorIsInvalid() throws Exception {
    String json = "{\"interval\":\"1h\",\"aggregator\":\"no such agg\"}";
    Downsampler downsampler = JSON.parseToObject(json, Downsampler.class);
    downsampler.validate();
  }
  
  @Test
  public void deserialize() throws Exception {
    String json = "{\"interval\":\"1h\",\"aggregator\":\"zimsum\"}";
    Downsampler downsampler = JSON.parseToObject(json, Downsampler.class);
    downsampler.validate();
    Downsampler expected = Downsampler.newBuilder()
        .setInterval("1h").setAggregator("zimsum").build();
    assertEquals(expected, downsampler);
    
    json = "{\"interval\":\"1h\",\"aggregator\":\"zimsum\","
        + "\"fillPolicy\":{\"policy\":\"nan\"},\"junkfield\":true}";
    downsampler = JSON.parseToObject(json, Downsampler.class);
    downsampler.validate();
    expected = Downsampler.newBuilder()
        .setInterval("1h").setAggregator("zimsum")
        .setFillPolicy(new NumericFillPolicy(FillPolicy.NOT_A_NUMBER)).build();
    assertEquals(expected, downsampler);
  }
  
  @Test
  public void serialize() throws Exception {
    Downsampler downsampler = Downsampler.newBuilder()
        .setInterval("1h").setAggregator("zimsum").build();
    String json = JSON.serializeToString(downsampler);
    assertTrue(json.contains("\"interval\":\"1h\""));
    assertTrue(json.contains("\"aggregator\":\"zimsum\""));
    
    downsampler = Downsampler.newBuilder()
        .setInterval("15m").setAggregator("max")
        .setFillPolicy(new NumericFillPolicy(FillPolicy.NOT_A_NUMBER)).build();
    json = JSON.serializeToString(downsampler);
    assertTrue(json.contains("\"interval\":\"15m\""));
    assertTrue(json.contains("\"aggregator\":\"max\""));
    assertTrue(json.contains("\"fillPolicy\":{"));
    assertTrue(json.contains("\"policy\":\"nan\""));
  }

  @Test
  public void hashCodeEqualsCompareTo() throws Exception {
    final Downsampler ds1 = new Downsampler.Builder()
        .setAggregator("sum")
        .setInterval("1h")
        .setFillPolicy(new NumericFillPolicy.Builder()
            .setPolicy(FillPolicy.NOT_A_NUMBER).build())
        .build();
    
    Downsampler ds2 = new Downsampler.Builder()
        .setAggregator("sum")
        .setInterval("1h")
        .setFillPolicy(new NumericFillPolicy.Builder()
            .setPolicy(FillPolicy.NOT_A_NUMBER).build())
        .build();
    assertEquals(ds1.hashCode(), ds2.hashCode());
    assertEquals(ds1, ds2);
    assertEquals(0, ds1.compareTo(ds2));
    
    ds2 = new Downsampler.Builder()
        .setAggregator("max")  // <-- diff
        .setInterval("1h")
        .setFillPolicy(new NumericFillPolicy.Builder()
            .setPolicy(FillPolicy.NOT_A_NUMBER).build())
        .build();
    assertNotEquals(ds1.hashCode(), ds2.hashCode());
    assertNotEquals(ds1, ds2);
    assertEquals(1, ds1.compareTo(ds2));
    
    ds2 = new Downsampler.Builder()
        .setAggregator("sum")
        .setInterval("30m")  // <-- diff
        .setFillPolicy(new NumericFillPolicy.Builder()
            .setPolicy(FillPolicy.NOT_A_NUMBER).build())
        .build();
    assertNotEquals(ds1.hashCode(), ds2.hashCode());
    assertNotEquals(ds1, ds2);
    assertEquals(-1, ds1.compareTo(ds2));
    
    ds2 = new Downsampler.Builder()
        .setAggregator("sum")
        .setInterval("1h")
        .setFillPolicy(new NumericFillPolicy.Builder()
            .setPolicy(FillPolicy.ZERO).build())   // <-- diff
        .build();
    assertNotEquals(ds1.hashCode(), ds2.hashCode());
    assertNotEquals(ds1, ds2);
    assertEquals(-1, ds1.compareTo(ds2));
    
    ds2 = new Downsampler.Builder()
        .setAggregator("sum")
        .setInterval("1h")
        //.setFillPolicy(new NumericFillPolicy.Builder() // <-- diff
        //    .setPolicy(FillPolicy.ZERO).build())   
        .build();
    assertNotEquals(ds1.hashCode(), ds2.hashCode());
    assertNotEquals(ds1, ds2);
    assertEquals(1, ds1.compareTo(ds2));
  }
}