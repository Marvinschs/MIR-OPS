package com.marvinschs.mirops.frontend.render.types

interface IResizable: IElement {
    
    fun onSizeChanged(newWidth: Float, newHeight: Float): IResizable {
        return this@IResizable
    }
    
    fun setX(x: Float): IResizable {
        return this@IResizable
    }
    
    fun setY(y: Float): IResizable {
        return this@IResizable
    }
    
    fun setWidth(newWidth: Float): IResizable {
        return this@IResizable
    }
    
    fun setHeight(newHeight: Float): IResizable {
        return this@IResizable
    }
}