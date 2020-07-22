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
sphereIntersectAt_partial(__global double *res, __global const double *r,
                        __global const double *b,
                        __global const double *c,
                        __global const double *g,
                        __global const double *d,
                        const int resOffset, const int rOffset,
                        const int bOffset, const int cOffset, const int gOffset,
                        const int dOffset) {

    // double discriminant = (b[bOffset] * b[bOffset]) - (g[gOffset]) * (c[cOffset] - 1);
    double discriminant = d[dOffset];

    if (discriminant < 0) {
        res[resOffset] = -1;
        return;
    }

    double discriminantSqrt = sqrt(discriminant);

    double t[2];

    t[0] = (-b[bOffset] + discriminantSqrt) / (g[gOffset]);
    t[1] = (-b[bOffset] - discriminantSqrt) / (g[gOffset]);

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
sphereIntersectAt_else(__global double *res, __global const double *r,
                        __global double *t,
                        const int resOffset, const int rOffset,
                        const int tOffset) {
//        double discriminantSqrt = d[dOffset];

//        t[tOffset] = (-b[bOffset] + discriminantSqrt) / (g[gOffset]);
//        t[tOffset + 1] = (-b[bOffset] - discriminantSqrt) / (g[gOffset]);

        if (t[tOffset] > 0 && t[tOffset + 1] > 0) {
            if (t[tOffset] < t[tOffset + 1]) {
                res[resOffset] = t[tOffset];
            } else {
                res[resOffset] = t[tOffset + 1];
            }
        } else {
            if (t[tOffset] > 0) {
                res[resOffset] = t[tOffset];
            } else if (t[tOffset + 1] > 0) {
                res[resOffset] = t[tOffset + 1];
            } else {
                res[resOffset] = -1;
            }
        }
}