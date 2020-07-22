__kernel void
pinholeCameraRayAt(__global double *res, __global const double *pos, __global const double *sd,
                    __global const double *rand,
                    __global const double *l, __global const double *pd, __global const double *bl,
                    __global const double *fl,
                    __global const double *u, __global const double *v, __global const double *w,
                    const int resOffset, const int posOffset, const int sdOffset, const int randOffset,
                    const int lOffset, const int pdOffset, const int blOffset, const int flOffset,
                    const int uOffset, const int vOffset, const int wOffset) {
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
pinholeCameraRayAt_partial(__global double *res, __global const double *pos, __global const double *sd,
                    __global const double *pqr,
                    __global const double *l, __global const double *bl,
                    const int resOffset, const int posOffset, const int sdOffset,
                    const int pqrOffset,
                    const int lOffset, const int blOffset) {
    res[resOffset] = l[lOffset];
    res[resOffset + 1] = l[lOffset + 1];
    res[resOffset + 2] = l[lOffset + 2];
    res[resOffset + 3] = pqr[pqrOffset];
    res[resOffset + 4] = pqr[pqrOffset + 1];
    res[resOffset + 5] = pqr[pqrOffset + 2];
}