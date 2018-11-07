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