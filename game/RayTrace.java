import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

/***************************************************
*
*   An instructional Ray-Tracing Renderer written
*   for MIT 6.837  Fall '98 by Leonard McMillan.
*
*   A fairly primitive Ray-Tracing program written
*   on a Sunday afternoon before Monday's class.
*   Everything is contained in a single file. The
*   structure should be fairly easy to extend, with
*   new primitives, features and other such stuff.
*
*   I tend to write things bottom up (old K&R C
*   habits die slowly). If you want the big picture
*   scroll to the applet code at the end and work
*   your way back here.
*
****************************************************/

// A simple vector class
class Vector3D {
    public float x, y, z;

    // constructors
    public Vector3D( ) {
    }

    public Vector3D(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Vector3D(Vector3D v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    // methods
    public final float dot(Vector3D B) {
        return (x*B.x + y*B.y + z*B.z);
    }

    public final float dot(float Bx, float By, float Bz) {
        return (x*Bx + y*By + z*Bz);
    }

    public static final float dot(Vector3D A, Vector3D B) {
        return (A.x*B.x + A.y*B.y + A.z*B.z);
    }

    public final Vector3D cross(Vector3D B) {
        return new Vector3D(y*B.z - z*B.y, z*B.x - x*B.z, x*B.y - y*B.x);
    }

    public final Vector3D cross(float Bx, float By, float Bz) {
        return new Vector3D(y*Bz - z*By, z*Bx - x*Bz, x*By - y*Bx);
    }

    public final static Vector3D cross(Vector3D A, Vector3D B) {
        return new Vector3D(A.y*B.z - A.z*B.y, A.z*B.x - A.x*B.z, A.x*B.y - A.y*B.x);
    }

    public final float length( ) {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public final static float length(Vector3D A) {
        return (float) Math.sqrt(A.x*A.x + A.y*A.y + A.z*A.z);
    }

    public final void normalize( ) {
        float t = x*x + y*y + z*z;
        if (t != 0 && t != 1) t = (float) (1 / Math.sqrt(t));
        x *= t;
        y *= t;
        z *= t;
    }

    public final static Vector3D normalize(Vector3D A) {
        float t = A.x*A.x + A.y*A.y + A.z*A.z;
        if (t != 0 && t != 1) t = (float)(1 / Math.sqrt(t));
        return new Vector3D(A.x*t, A.y*t, A.z*t);
    }

    public String toString() {
        return new String("["+x+", "+y+", "+z+"]");
    }
}


class Ray {
    public static final float MAX_T = Float.MAX_VALUE;
    Vector3D origin;
    Vector3D direction;
    float t;
    Renderable object;

    public Ray(Vector3D eye, Vector3D dir) {
        origin = new Vector3D(eye);
        direction = Vector3D.normalize(dir);
    }

    public boolean trace(Vector objects) {
        Enumeration objList = objects.elements();
        t = MAX_T;
        object = null;
        while (objList.hasMoreElements()) {
            Renderable object = (Renderable) objList.nextElement();
            object.intersect(this);
        }
        return (object != null);
    }

    // The following method is not strictly needed, and most likely
    // adds unnecessary overhead, but I prefered the syntax
    //
    //            ray.Shade(...)
    // to
    //            ray.object.Shade(ray, ...)
    //
    public final Color Shade(Vector lights, Vector objects, Color bgnd) {
        return object.Shade(this, lights, objects, bgnd);
    }

    public String toString() {
        return ("ray origin = "+origin+"  direction = "+direction+"  t = "+t);
    }
}

// All the public variables here are ugly, but I
// wanted Lights and Surfaces to be "friends"
class Light {
    public static final int AMBIENT = 0;
    public static final int DIRECTIONAL = 1;
    public static final int POINT = 2;

    public int lightType;
    public Vector3D lvec;           // the position of a point light or
                                    // the direction to a directional light
    public float ir, ig, ib;        // intensity of the light source

    public Light(int type, Vector3D v, float r, float g, float b) {
        lightType = type;
        ir = r;
        ig = g;
        ib = b;
        if (type != AMBIENT) {
            lvec = v;
            if (type == DIRECTIONAL) {
                lvec.normalize();
            }
        }
    }
}


class Surface {
    public float ir, ig, ib;        // surface's intrinsic color
    public float ka, kd, ks, ns;    // constants for phong model
    public float kt, kr, nt;
    private static final float TINY = 0.001f;
    private static final float I255 = 0.00392156f;  // 1/255

    public Surface(float rval, float gval, float bval, float a, float d, float s, float n, float r, float t, float index) {
        ir = rval; ig = gval; ib = bval;
        ka = a; kd = d; ks = s; ns = n;
        kr = r*I255; kt = t; nt = index;
    }

    public Color Shade(Vector3D p, Vector3D n, Vector3D v, Vector lights, Vector objects, Color bgnd) {
        Enumeration lightSources = lights.elements();

        float r = 0;
        float g = 0;
        float b = 0;
        while (lightSources.hasMoreElements()) {
            Light light = (Light) lightSources.nextElement();
            if (light.lightType == Light.AMBIENT) {
                r += ka*ir*light.ir;
                g += ka*ig*light.ig;
                b += ka*ib*light.ib;
            } else {
                Vector3D l;
                if (light.lightType == Light.POINT) {
                    l = new Vector3D(light.lvec.x - p.x, light.lvec.y - p.y, light.lvec.z - p.z);
                    l.normalize();
                } else {
                    l = new Vector3D(-light.lvec.x, -light.lvec.y, -light.lvec.z);
                }

                // Check if the surface point is in shadow
                Vector3D poffset = new Vector3D(p.x + TINY*l.x, p.y + TINY*l.y, p.z + TINY*l.z);
                Ray shadowRay = new Ray(poffset, l);
                if (shadowRay.trace(objects))
                    break;

                float lambert = Vector3D.dot(n,l);
                if (lambert > 0) {
                    if (kd > 0) {
                        float diffuse = kd*lambert;
                        r += diffuse*ir*light.ir;
                        g += diffuse*ig*light.ig;
                        b += diffuse*ib*light.ib;
                    }
                    if (ks > 0) {
                        lambert *= 2;
                        float spec = v.dot(lambert*n.x - l.x, lambert*n.y - l.y, lambert*n.z - l.z);
                        if (spec > 0) {
                            spec = ks*((float) Math.pow((double) spec, (double) ns));
                            r += spec*light.ir;
                            g += spec*light.ig;
                            b += spec*light.ib;
                        }
                    }
                }
            }
        }

        // Compute illumination due to reflection
        if (kr > 0) {
            float t = v.dot(n);
            if (t > 0) {
                t *= 2;
                Vector3D reflect = new Vector3D(t*n.x - v.x, t*n.y - v.y, t*n.z - v.z);
                Vector3D poffset = new Vector3D(p.x + TINY*reflect.x, p.y + TINY*reflect.y, p.z + TINY*reflect.z);
                Ray reflectedRay = new Ray(poffset, reflect);
                if (reflectedRay.trace(objects)) {
                    Color rcolor = reflectedRay.Shade(lights, objects, bgnd);
                    r += kr*rcolor.getRed();
                    g += kr*rcolor.getGreen();
                    b += kr*rcolor.getBlue();
                } else {
                    r += kr*bgnd.getRed();
                    g += kr*bgnd.getGreen();
                    b += kr*bgnd.getBlue();
                }
            }
        }

        // Add code for refraction here

        r = (r > 1f) ? 1f : r;
        g = (g > 1f) ? 1f : g;
        b = (b > 1f) ? 1f : b;
        return new Color(r, g, b);
    }
}


// An object must implement a Renderable interface in order to
// be ray traced. Using this interface it is straight forward
// to add new objects
abstract interface Renderable {
    public abstract boolean intersect(Ray r);
    public abstract Color Shade(Ray r, Vector lights, Vector objects, Color bgnd);
    public String toString();
}

// An example "Renderable" object
class Sphere implements Renderable {
    Surface surface;
    Vector3D center;
    float radius;
    float radSqr;

    public Sphere(Surface s, Vector3D c, float r) {
        surface = s;
        center = c;
        radius = r;
        radSqr = r*r;
    }

    public boolean intersect(Ray ray) {
        float dx = center.x - ray.origin.x;
        float dy = center.y - ray.origin.y;
        float dz = center.z - ray.origin.z;
        float v = ray.direction.dot(dx, dy, dz);

        // Do the following quick check to see if there is even a chance
        // that an intersection here might be closer than a previous one
        if (v - radius > ray.t)
            return false;

        // Test if the ray actually intersects the sphere
        float t = radSqr + v*v - dx*dx - dy*dy - dz*dz;
        if (t < 0)
            return false;

        // Test if the intersection is in the positive
        // ray direction and it is the closest so far
        t = v - ((float) Math.sqrt(t));
        if ((t > ray.t) || (t < 0))
            return false;

        ray.t = t;
        ray.object = this;
        return true;
    }

    public Color Shade(Ray ray, Vector lights, Vector objects, Color bgnd) {
        // An object shader doesn't really do too much other than
        // supply a few critical bits of geometric information
        // for a surface shader. It must must compute:
        //
        //   1. the point of intersection (p)
        //   2. a unit-length surface normal (n)
        //   3. a unit-length vector towards the ray's origin (v)
        //
        float px = ray.origin.x + ray.t*ray.direction.x;
        float py = ray.origin.y + ray.t*ray.direction.y;
        float pz = ray.origin.z + ray.t*ray.direction.z;

        Vector3D p = new Vector3D(px, py, pz);
        Vector3D v = new Vector3D(-ray.direction.x, -ray.direction.y, -ray.direction.z);
        Vector3D n = new Vector3D(px - center.x, py - center.y, pz - center.z);
        n.normalize();

        // The illumination model is applied
        // by the surface's Shade() method
        return surface.Shade(p, n, v, lights, objects, bgnd);
    }

    public String toString() {
        return ("sphere "+center+" "+radius);
    }
}


//    The following Applet demonstrates a simple ray tracer with a
//    mouse-based painting interface for the impatient and Mac owners
public class RayTrace extends Applet implements Runnable {
    final static int CHUNKSIZE = 100;
    Image screen;
    Graphics gc;
    Vector objectList;
    Vector lightList;
    Surface currentSurface;

    Vector3D eye, lookat, up;
    Vector3D Du, Dv, Vp;
    float fov;

    Color background;

    int width, height;

    public void init( ) {
        // initialize the off-screen rendering buffer
        width = size().width;
        height = size().height;
        screen = createImage(width, height);
        gc = screen.getGraphics();
        gc.setColor(getBackground());
        gc.fillRect(0, 0, width, height);

        fov = 30;               // default horizonal field of view

        // Initialize various lists
        objectList = new Vector(CHUNKSIZE, CHUNKSIZE);
        lightList = new Vector(CHUNKSIZE, CHUNKSIZE);
        currentSurface = new Surface(0.8f, 0.2f, 0.9f, 0.2f, 0.4f, 0.4f, 10.0f, 0f, 0f, 1f);

        // Parse the scene file
        String filename = getParameter("datafile");
        showStatus("Parsing " + filename);
        InputStream is = null;
        try {
            is = new URL(getDocumentBase(), filename).openStream();
            ReadInput(is);
            is.close();
        } catch (IOException e) {
        		e.printStackTrace();
            showStatus("Error reading "+filename);
            System.err.println("Error reading "+filename);
            System.exit(-1);
        }

        // Initialize more defaults if they weren't specified
        if (eye == null) eye = new Vector3D(0, 0, 10);
        if (lookat == null) lookat = new Vector3D(0, 0, 0);
        if (up  == null) up = new Vector3D(0, 1, 0);
        if (background == null) background = new Color(0,0,0);

        // Compute viewing matrix that maps a
        // screen coordinate to a ray direction
        Vector3D look = new Vector3D(lookat.x - eye.x, lookat.y - eye.y, lookat.z - eye.z);
        Du = Vector3D.normalize(look.cross(up));
        Dv = Vector3D.normalize(look.cross(Du));
        float fl = (float)(width / (2*Math.tan((0.5*fov)*Math.PI/180)));
        Vp = Vector3D.normalize(look);
        Vp.x = Vp.x*fl - 0.5f*(width*Du.x + height*Dv.x);
        Vp.y = Vp.y*fl - 0.5f*(width*Du.y + height*Dv.y);
        Vp.z = Vp.z*fl - 0.5f*(width*Du.z + height*Dv.z);
    }


    double getNumber(StreamTokenizer st) throws IOException {
        if (st.nextToken() != StreamTokenizer.TT_NUMBER) {
            System.err.println("ERROR: number expected in line "+st.lineno());
            throw new IOException(st.toString());
        }
        return st.nval;
    }

    void ReadInput(InputStream is) throws IOException {
	    StreamTokenizer st = new StreamTokenizer(is);
    	st.commentChar('#');
        scan: while (true) {
	        switch (st.nextToken()) {
	          default:
		        break scan;
	          case StreamTokenizer.TT_WORD:
		        	System.out.println(st.nextToken());
	            if (st.sval.equals("sphere")) {
                    Vector3D v = new Vector3D((float) getNumber(st), (float) getNumber(st), (float) getNumber(st));
		            float r = (float) getNumber(st);
		            objectList.addElement(new Sphere(currentSurface, v, r));
			    } else
			    if (st.sval.equals("eye")) {
		            eye = new Vector3D((float) getNumber(st), (float) getNumber(st), (float) getNumber(st));
			    } else
			    if (st.sval.equals("lookat")) {
		            lookat = new Vector3D((float) getNumber(st), (float) getNumber(st), (float) getNumber(st));
			    } else
			    if (st.sval.equals("up")) {
		            up = new Vector3D((float) getNumber(st), (float) getNumber(st), (float) getNumber(st));
			    } else
			    if (st.sval.equals("fov")) {
                    fov = (float) getNumber(st);
			    } else
			    if (st.sval.equals("background")) {
                    background = new Color((float) getNumber(st), (float) getNumber(st), (float) getNumber(st));
			    } else
			    if (st.sval.equals("light")) {
			        float r = (float) getNumber(st);
			        float g = (float) getNumber(st);
			        float b = (float) getNumber(st);
		            if (st.nextToken() != StreamTokenizer.TT_WORD) {
                        throw new IOException(st.toString());
                    }
		            if (st.sval.equals("ambient")) {
		                lightList.addElement(new Light(Light.AMBIENT, null, r, g, b));
		            } else
		            if (st.sval.equals("directional")) {
		                Vector3D v = new Vector3D((float) getNumber(st), (float) getNumber(st), (float) getNumber(st));
		                lightList.addElement(new Light(Light.DIRECTIONAL, v, r, g, b));
		            } else
		            if (st.sval.equals("point")) {
		                Vector3D v = new Vector3D((float) getNumber(st), (float) getNumber(st), (float) getNumber(st));
		                lightList.addElement(new Light(Light.POINT, v, r, g, b));
		            } else {
		                System.err.println("ERROR: in line "+st.lineno()+" at "+st.sval);
		                throw new IOException(st.toString());
		            }
			    } else
			    if (st.sval.equals("surface")) {
			        float r = (float) getNumber(st);
			        float g = (float) getNumber(st);
			        float b = (float) getNumber(st);
		            float ka = (float) getNumber(st);
		            float kd = (float) getNumber(st);
		            float ks = (float) getNumber(st);
		            float ns = (float) getNumber(st);
		            float kr = (float) getNumber(st);
		            float kt = (float) getNumber(st);
		            float index = (float) getNumber(st);
		            currentSurface = new Surface(r, g, b, ka, kd, ks, ns, kr, kt, index);
			    }
			    break;
	        }
	    }
        is.close();
	    if (st.ttype != StreamTokenizer.TT_EOF)
	        throw new IOException(st.toString());
	}
    
    boolean finished = false;
    
    public void paint(Graphics g) {
        if (finished)
            g.drawImage(screen, 0, 0, this);
    }

    // this overide avoid the unnecessary clear on each paint()
    public void update(Graphics g) {
        paint(g);
    }


    Thread raytracer;

    public void start() {
        if (raytracer == null) {
            raytracer = new Thread(this);
            raytracer.start();
        } else {
            raytracer.resume();
        }
    }

    public void stop() {
        if (raytracer != null) {
            raytracer.suspend();
        }
    }

    private void renderPixel(int i, int j) {
        Vector3D dir = new Vector3D(
                                i*Du.x + j*Dv.x + Vp.x,
                                i*Du.y + j*Dv.y + Vp.y,
                                i*Du.z + j*Dv.z + Vp.z);
        Ray ray = new Ray(eye, dir);
        if (ray.trace(objectList)) {
            gc.setColor(ray.Shade(lightList, objectList, background));
        } else {
            gc.setColor(background);
        }
        gc.drawLine(i, j, i, j);        // oh well, it works.
    }

    public void run() {
        Graphics g = getGraphics();
        long time = System.currentTimeMillis();
        for (int j = 0; j < height; j++) {
            showStatus("Scanline "+j);
            for (int i = 0; i < width; i++) {
                renderPixel(i, j);
            }
            g.drawImage(screen, 0, 0, this);        // doing this less often speed things up a bit
        }
        g.drawImage(screen, 0, 0, this);
        time = System.currentTimeMillis() - time;
        showStatus("Rendered in "+(time/60000)+":"+((time%60000)*0.001));
        finished = true;
    }


    public boolean mouseDown(Event e, int x, int y) {
        renderPixel(x, y);
        repaint();
        return true;
    }

    public boolean mouseDrag(Event e, int x, int y) {
        renderPixel(x, y);
        repaint();
        return true;
    }

    public boolean mouseUp(Event e, int x, int y) {
        renderPixel(x, y);
        repaint();
        return true;
    }
}