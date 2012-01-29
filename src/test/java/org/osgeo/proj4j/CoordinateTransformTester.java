package org.osgeo.proj4j;

import java.text.DecimalFormat;


public class CoordinateTransformTester 
{
  boolean verbose = true;
  
	private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
  CRSFactory crsFactory = new CRSFactory();

  static final String WGS84_PARAM = "+title=long/lat:WGS84 +proj=longlat +datum=WGS84 +units=degrees";
  CoordinateReferenceSystem WGS84 = crsFactory.createFromParameters("WGS84", WGS84_PARAM);

  public CoordinateTransformTester(boolean verbose) {
    this.verbose = verbose;
  }

  private ProjCoordinate p = new ProjCoordinate();
  private ProjCoordinate p2 = new ProjCoordinate();

  public boolean checkTransformFromWGS84(String name, double lon, double lat, double x, double y)
  {
    return checkTransformFromWGS84(name, lon, lat, x, y, 0.0001);
  }
  
  public boolean checkTransformFromWGS84(String name, double lon, double lat, double x, double y, double tolerance)
  {
    return checkTransform(WGS84, lon, lat, createCRS(name), x, y, tolerance);
  }
  
  public boolean checkTransformToWGS84(String name, double x, double y, double lon, double lat, double tolerance)
  {
    return checkTransform(createCRS(name), x, y, WGS84, lon, lat, tolerance);
  }
  
  public boolean checkTransformFromGeo(String name, double lon, double lat, double x, double y, double tolerance)
  {
    CoordinateReferenceSystem  crs = createCRS(name);
    CoordinateReferenceSystem geoCRS = crs.createGeographic();
    return checkTransform(geoCRS, lon, lat, crs, x, y, tolerance);
  }
  
  public boolean checkTransformToGeo(String name, double x, double y, double lon, double lat, double tolerance)
  {
    CoordinateReferenceSystem  crs = createCRS(name);
    CoordinateReferenceSystem geoCRS = crs.createGeographic();
    return checkTransform(crs, x, y, geoCRS, lon, lat, tolerance);
  }
  
  private CoordinateReferenceSystem createCRS(String crsSpec)
  {
    CoordinateReferenceSystem crs = null;
    // test if name is a PROJ4 spec
    if (crsSpec.indexOf("+") >= 0) {
      crs = crsFactory.createFromParameters("Anon", crsSpec);
    } 
    else {
      crs = crsFactory.createFromName(crsSpec);
    }
    return crs;
  }
  
  private static String crsDisplay(CoordinateReferenceSystem crs)
  {
    return crs.getName() 
      + "(" + crs.getProjection()+"/" + crs.getDatum().getCode() + ")";
  }
  
  public boolean checkTransform(
  		String srcCRS, double x1, double y1, 
  		String tgtCRS, double x2, double y2, double tolerance)
  {
    return checkTransform(
    		createCRS(srcCRS), x1, y1, 
    		createCRS(tgtCRS), x2, y2, tolerance);
  }
  
  public boolean checkTransform(
  		CoordinateReferenceSystem srcCRS, double x1, double y1, 
  		CoordinateReferenceSystem tgtCRS, double x2, double y2, 
  		double tolerance)
  {
    p.x = x1;
    p.y = y1;
    CoordinateTransform trans = ctFactory.createTransform(srcCRS, tgtCRS);
    trans.transform(p, p2);
    
    if (verbose) {
      System.out.println(crsDisplay(srcCRS) + " => " + crsDisplay(tgtCRS) );
      System.out.println(
      		p.toShortString() 
          + " -> " 
          + p2.toShortString()
          + " (expected: " + (new ProjCoordinate(x2, y2)).toShortString() + " )"
          );
    }
    
    double dx = Math.abs(p2.x - x2);
    double dy = Math.abs(p2.y - y2);
    double dd = Math.max(dx, dy);
    boolean isInTol =  dd <= tolerance;
   
    if (verbose && ! isInTol) {
      System.out.println("FAIL - discrepancy = " + dd + " > tolerance = " + tolerance);
      System.out.println("Src CRS: " + srcCRS.getParameterString());
      System.out.println("Tgt CRS: " + tgtCRS.getParameterString());
   }

    if (verbose) {
      System.out.println();
    }

    return isInTol;
  }
  
  public boolean checkTransform(
  		String cs1, double x1, double y1, 
  		String cs2, double x2, double y2, 
  		double tolerance,
  		boolean checkInverse)
  {
  	boolean isOkForward = checkTransform(cs1, x1, y1, cs2, x2, y2, tolerance);
  	boolean isOkInverse = true;
  	if (checkInverse)
  		isOkInverse = checkTransform(cs2, x2, y2, cs1, x1, y1, tolerance);
  	
  	return isOkForward && isOkInverse;
  }
}
