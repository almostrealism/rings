/*
 * Copyright (C) 2005  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package com.almostrealism.util.graphics;

import com.almostrealism.util.Producer;

/**
 * ColorProducer is implemented by any class that can produce an RGB object
 * given some array of input objects.
 * 
 * @author Mike Murray
 */
public interface ColorProducer extends Producer {
    /**
     * Produces a color using the specified arguments.
     * 
     * @param args  Arguments.
     * @return  The RGB color produced.
     */
    public RGB evaluate(Object args[]);
}
