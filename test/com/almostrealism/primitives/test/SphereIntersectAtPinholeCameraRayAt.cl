__kernel void
pinholeCameraRayAt(__global double *res, __global const double *pos, __global const double *sd,
                    __global const double *rand,
                    __global const double *l, __global const double *pd, __global const double *bl,
                    __global const double *fl, __global const double *u, __global const double *v, __global const double *w,
                    const int resOffset, const int posOffset, const int sdOffset, const int randOffset,
                    const int lOffset, const int pdOffset, const int blOffset,
                    const int flOffset, const int uOffset, const int vOffset, const int wOffset) {
    double bu = pd[pdOffset] / 2;
    double bv = pd[pdOffset + 1] / 2;
    double au = -bu;
    double av = -bv;

    double p = au + (bu - au) * (pos[posOffset] / (sd[sdOffset] - 1));
    double q = av + (bv - av) * (pos[posOffset + 1] / (sd[sdOffset + 1] - 1));
    double r = -fl[flOffset];

    res[resOffset + 3] = p * u[uOffset] + q * v[vOffset] + r * w[wOffset];
    res[resOffset + 4] = p * u[uOffset + 1] + q * v[vOffset + 1] + r * w[wOffset + 1];
    res[resOffset + 5] = p * u[uOffset + 2] + q * v[vOffset + 2] + r * w[wOffset + 2];

    double len = sqrt(res[resOffset + 3] * res[resOffset + 3] +
                    res[resOffset + 4] * res[resOffset + 4] +
                    res[resOffset + 5] * res[resOffset + 5]);

    if (bl[blOffset] != 0.0 || bl[blOffset + 1] != 0.0) {
        double wx = res[resOffset + 3];
        double wy = res[resOffset + 4];
        double wz = res[resOffset + 5];

        double tx = res[resOffset + 3];
        double ty = res[resOffset + 4];
        double tz = res[resOffset + 5];

        if (tx < ty && ty < tz) {
            tx = 1.0;
        } else if (ty < tx && ty < tz) {
            ty = 1.0;
        } else {
            tz = 1.0;
        }

        double wl = sqrt(wx * wx + wy * wy + wz * wz);
        wx = wx / wl;
        wy = wy / wl;
        wz = wz / wl;

        double ux = ty * wz - tz * wy;
        double uy = tz * wx - tx * wz;
        double uz = tx * wy - ty * wx;

        double ul = sqrt(ux * ux + uy * uy + uz * uz);
        ux = ux / ul;
        uy = uy / ul;
        uz = uz / ul;

        double vx = wy * uz - wz * uy;
        double vy = wz * ux - wx * uz;
        double vz = wx * uy - wy * ux;

        res[resOffset + 3] = res[resOffset + 3] +
                            ux * bl[blOffset] * (rand[randOffset] - 0.5) +
                            vx * bl[blOffset + 1] * (rand[randOffset + 1] - 0.5);
        res[resOffset + 4] = res[resOffset + 4] +
                            uy * bl[blOffset] * (rand[randOffset] - 0.5) +
                            vy * bl[blOffset + 1] * (rand[randOffset + 1] - 0.5);
        res[resOffset + 5] = res[resOffset + 5] +
                            uz * bl[blOffset] * (rand[randOffset] - 0.5) +
                            vz * bl[blOffset + 1] * (rand[randOffset + 1] - 0.5);

        double dl = sqrt(res[resOffset + 3] * res[resOffset + 3] +
                        res[resOffset + 4] * res[resOffset + 4] +
                        res[resOffset + 5] * res[resOffset + 5]);

        double d = len / dl;
        res[resOffset + 3] = res[resOffset + 3] * d;
        res[resOffset + 4] = res[resOffset + 4] * d;
        res[resOffset + 5] = res[resOffset + 5] * d;
    }

    res[resOffset] = l[lOffset];
    res[resOffset + 1] = l[lOffset + 1];
    res[resOffset + 2] = l[lOffset + 2];
}

__kernel void
sphereIntersectAt(__global double *res, __global const double *r,
                    const int resOffset, const int rOffset) {
    __local double r_l[6];

    rayCopy_globalToLocal(r_l, r, 0, rOffset);

    __local double b[1];
    __local double c[1];
    __local double g[1];

    rayODotD_local(b, r_l, 0, 0);
    rayODotO_local(c, r_l, 0, 0);
    rayDDotD_local(g, r_l, 0, 0);

    double discriminant = (b[0] * b[0]) - (g[0]) * (c[0] - 1);

    if (discriminant < 0) {
        res[resOffset] = -1;
        return;
    }

    double discriminantSqrt = sqrt(discriminant);

    double t[2];

    t[0] = (-b[0] + discriminantSqrt) / (g[0]);
    t[1] = (-b[0] - discriminantSqrt) / (g[0]);

    if (t[0] > 0 && t[1] > 0) {
        if (t[0] < t[1]) {
            res[resOffset] = t[0];
        } else {
            res[resOffset] = t[1];
        }
    } else if (t[0] > 0) {
        res[resOffset] = t[0];
    } else if (t[1] > 0) {
        res[resOffset] = t[1];
    } else {
        res[resOffset] = -1;
    }
}

__kernel void
sphereIntersectAt_pinholeCameraRayAt(__global double *res, __global const double *pos, __global const double *sd,
                    __global const double *rand,
                    __global const double *l, __global const double *pd, __global const double *bl,
                    __global const double *fl, __global const double *u, __global const double *v, __global const double *w,
                    const int resOffset, const int posOffset, const int sdOffset, const int randOffset,
                    const int lOffset, const int pdOffset, const int blOffset,
                    const int flOffset, const int uOffset, const int vOffset, const int wOffset) {

    /* Pinhole Camera rayAt */

    // double bu = (pd[pdOffset] / 2);
    // double bv = pd[pdOffset + 1] / 2;
    // double au = -bu;
    // double av = -bv;

    double p = -(pd[pdOffset] / 2) + ((pd[pdOffset] / 2) + (pd[pdOffset] / 2)) * (pos[posOffset] / (sd[sdOffset] - 1));
    double q = -(pd[pdOffset + 1] / 2) + ((pd[pdOffset + 1] / 2) + (pd[pdOffset + 1] / 2)) * (pos[posOffset + 1] / (sd[sdOffset + 1] - 1));
    double r = -fl[flOffset];

    res[resOffset + 3] = p * u[uOffset] + q * v[vOffset] + r * w[wOffset];
    res[resOffset + 4] = p * u[uOffset + 1] + q * v[vOffset + 1] + r * w[wOffset + 1];
    res[resOffset + 5] = p * u[uOffset + 2] + q * v[vOffset + 2] + r * w[wOffset + 2];

    double len = sqrt(res[resOffset + 3] * res[resOffset + 3] +
                    res[resOffset + 4] * res[resOffset + 4] +
                    res[resOffset + 5] * res[resOffset + 5]);

    if (bl[blOffset] != 0.0 || bl[blOffset + 1] != 0.0) {
        double wx = res[resOffset + 3];
        double wy = res[resOffset + 4];
        double wz = res[resOffset + 5];

        double tx = res[resOffset + 3];
        double ty = res[resOffset + 4];
        double tz = res[resOffset + 5];

        if (tx < ty && ty < tz) {
            tx = 1.0;
        } else if (ty < tx && ty < tz) {
            ty = 1.0;
        } else {
            tz = 1.0;
        }

        double wl = sqrt(wx * wx + wy * wy + wz * wz);
        wx = wx / wl;
        wy = wy / wl;
        wz = wz / wl;

        double ux = ty * wz - tz * wy;
        double uy = tz * wx - tx * wz;
        double uz = tx * wy - ty * wx;

        double ul = sqrt(ux * ux + uy * uy + uz * uz);
        ux = ux / ul;
        uy = uy / ul;
        uz = uz / ul;

        double vx = wy * uz - wz * uy;
        double vy = wz * ux - wx * uz;
        double vz = wx * uy - wy * ux;

        res[resOffset + 3] = res[resOffset + 3] +
                            ux * bl[blOffset] * (rand[randOffset] - 0.5) +
                            vx * bl[blOffset + 1] * (rand[randOffset + 1] - 0.5);
        res[resOffset + 4] = res[resOffset + 4] +
                            uy * bl[blOffset] * (rand[randOffset] - 0.5) +
                            vy * bl[blOffset + 1] * (rand[randOffset + 1] - 0.5);
        res[resOffset + 5] = res[resOffset + 5] +
                            uz * bl[blOffset] * (rand[randOffset] - 0.5) +
                            vz * bl[blOffset + 1] * (rand[randOffset + 1] - 0.5);

        double dl = sqrt(res[resOffset + 3] * res[resOffset + 3] +
                        res[resOffset + 4] * res[resOffset + 4] +
                        res[resOffset + 5] * res[resOffset + 5]);

        double d = len / dl;
        res[resOffset + 3] = res[resOffset + 3] * d;
        res[resOffset + 4] = res[resOffset + 4] * d;
        res[resOffset + 5] = res[resOffset + 5] * d;
    }

    res[resOffset] = l[lOffset];
    res[resOffset + 1] = l[lOffset + 1];
    res[resOffset + 2] = l[lOffset + 2];

    /* Sphere intersectAt */

    __local double r_l[6];

    rayCopy_globalToLocal(r_l, res, 0, resOffset);

    __local double b[1];
    __local double c[1];
    __local double g[1];

    rayODotD_local(b, r_l, 0, 0);
    rayODotO_local(c, r_l, 0, 0);
    rayDDotD_local(g, r_l, 0, 0);

    double discriminant = (b[0] * b[0]) - (g[0]) * (c[0] - 1);

    if (discriminant < 0) {
        res[resOffset] = -1;
        return;
    }

    double discriminantSqrt = sqrt(discriminant);

    double t[2];

    t[0] = (-b[0] + discriminantSqrt) / (g[0]);
    t[1] = (-b[0] - discriminantSqrt) / (g[0]);

    if (t[0] > 0 && t[1] > 0) {
        if (t[0] < t[1]) {
            res[resOffset] = t[0];
        } else {
            res[resOffset] = t[1];
        }
    } else if (t[0] > 0) {
        res[resOffset] = t[0];
    } else if (t[1] > 0) {
        res[resOffset] = t[1];
    } else {
        res[resOffset] = -1;
    }
}
