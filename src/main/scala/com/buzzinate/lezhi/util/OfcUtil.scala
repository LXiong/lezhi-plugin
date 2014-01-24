package com.buzzinate.lezhi.util

import java.util.Date
import java.text.NumberFormat
import com.buzzinate.common.util.DateTimeUtil

object OfcUtil {
    object ChartType extends Enumeration {
        type ChartType = Value
        val BAR, LINE, BAR_STACKED_2, LINE_MULTIPLE, LINE_AREA, LINE_FILL = Value
    }
    
    object PieChartType extends Enumeration {
        val PLATFORM_PIE, TYPE_PIE, NORMAL_PIE, LOCATION_PIE = Value
    }
    
    val COLORS: Array[String] = Array("#ff5c00", "#4f8a10", "#d01f3c", "#73880A", "#969696"
        , "#356aa0", "#6d6e71", "#D15600", "#6BBA70", "#3a3a3a", "#C79810" )
    
    val X_STEP_INTERVAL = 7.0
    val Y_STEP_INTERVAL = 10.0
    
    import ChartType._
    import PieChartType._
    
    /**
     * Convenience function with less parameters.
     * 
     * @param list
     * @param dateStart
     * @param dateEnd
     * @param type
     * @param isShowMenu
     * @return
     * @throws ParseException
     */
    def getOfc2ChartJsonData(list: Seq[(Date, Long)], dateStart:Date,
            dateEnd: Date, chartType: ChartType, isShowMenu: Boolean) : String = {
        return getOfc2ChartJsonData(list, dateStart, dateEnd, chartType, isShowMenu, null, null, false)
    }
    
    /**
     * Convenience function with less parameters.
     * 
     * @param list
     * @param dateStart
     * @param dateEnd
     * @param type
     * @param isShowMenu
     * @param key1
     * @param key2
     * @return
     * @throws ParseException
     */
    def getOfc2ChartJsonData(list: Seq[(Date, Long)], dateStart:Date,
            dateEnd: Date, chartType: ChartType, isShowMenu: Boolean, key1: String, key2: String) : String= {
        return getOfc2ChartJsonData(list, dateStart, dateEnd, chartType, isShowMenu, null, null, false) 
    }
    
    /**
     * 
     * Takes a List of Object[] and returns the json data for a OFC2 line chart.
     * 
     * @param list 
     *         Must be: 
     *         Object[
     *             Long (y-axis value for the count) for ChartType.BAR and ChartType.LINE charts,
     *             Long[2] for ChartType.BAR_STACKED_2 charts, or
     *             Long[int] for ChartType.LINE_MULTIPLE and ChartType.LINE_AREA charts
     *         , Date (date for x label)] 
     * @param dateStart the date to start the x axis
     * @param dateEnd the date to end the x axis
     * @param type The type of chart to generate. @see OFCUtil.ChartType
     * @param isShowMenu whether to show the flash chart menu or not
     * @param key1 the label for the first data point (ChartType.BAR_STACKED_2 type only)
     * @param key2 the label for the second data point (ChartType.BAR_STACKED_2 type only)
     * @param generateEmptyChart Whether to generate an empty chart (with all 0s) or not.
     * @return
     * @throws ParseException
     */
    def getOfc2ChartJsonData(list: Seq[(Date, AnyVal)], dateStart:Date,
            dateEnd: Date, chartType: ChartType, isShowMenu: Boolean, key1: String, key2: String, generateEmptyChart: Boolean): String = {
        // default value
        var jsonData = "{}" 
        if (list.nonEmpty || generateEmptyChart) {
            var value: AnyVal = 0
            var yMax: Long = 10
            var dataCount: Long = 0
            var date: Date = null
            var currentDate = dateStart
            var data = ""
            var xLabel = ""
            var bufData = new StringBuffer()
            var bufXLabel = new StringBuffer()
            var dateAppend = ""
            var valueAppend = ""
            // used for multiple line charts
            var numLines = 0
            var dataArray: Array[String] = null
            
            for (li <- list) {
                date = li._1
                value = li._2
                
                // add 0's to the data in the graph to when there is actually data.
                while (date.after(currentDate)) {
                    dateAppend = DateTimeUtil.formatDate(currentDate);
                    valueAppend = generateValue(chartType, 0, dateAppend);
                    bufXLabel.append(",'" + dateAppend + "'");
                    bufData.append("," + valueAppend);
                    currentDate = DateTimeUtil.subtractDays(currentDate, -1)
                    dataCount = dataCount + 1
                }
                
                currentDate = DateTimeUtil.subtractDays(currentDate, -1)
                dataCount = dataCount + 1

                dateAppend = DateTimeUtil.formatDate(date)
                valueAppend = generateValue(chartType, value, dateAppend)
                bufData.append("," + valueAppend)
                    
                bufXLabel.append(",'" + dateAppend + "'")
                if (value.isInstanceOf[Long]) {
                    if (value.asInstanceOf[Long] > yMax) yMax = value.asInstanceOf[Long]
                } else  if (value.isInstanceOf[Double]) {
                    if (value.asInstanceOf[Double] > yMax) yMax = value.asInstanceOf[Double].longValue()
                }
                
            }
            // add 0's to the data in the graph to the end date.
            while (currentDate.compareTo(dateEnd) <= 0) {
                dateAppend = DateTimeUtil.formatDate(currentDate)
                valueAppend = generateValue(chartType, 0, dateAppend)
                bufXLabel.append(",'" + dateAppend + "'")
                bufData.append("," + valueAppend)
                currentDate = DateTimeUtil.subtractDays(currentDate, -1)
                dataCount = dataCount + 1
            }
            
            data = bufData.toString()
            xLabel = bufXLabel.toString()
            if (data != null && !data.trim().equals("") && xLabel != null && 
                    !xLabel.trim().equals("")) {
                 jsonData = generateJsonDataLineChart(data.substring(1), xLabel
                            .substring(1), dataCount, yMax, "", isShowMenu, 0, chartType.equals(ChartType.LINE_FILL))
            }
        }
        return jsonData
    }
    
    /**
     * Generates a value for the data for OFC2.
     * 
     * @param type @see OFCUtil.ChartType
     * @param value 
     *         the value. Possible types:
     *             Long for ChartType.LINE or ChartType.BAR
     *             Long[2] for ChartType.BAR_STACKED_2
     *             Long[int] for ChartType.LINE_MULTIPLE and ChartType.LINE_AREA
     * @param labelx the x label
     * @return
     */
    def generateValue(chartType: ChartType, value: AnyVal, labelx: String) : String =  {
        var typeString = "solid-dot"
        var valName = "value"
        return "{'type':'" + typeString + "','" + valName + "':" + value + ",'tip':'" + labelx + 
                "<br>" + NumberFormat.getInstance().format(value) + "'}"
    }
    
    /**
     * Generates json data based on given data for an OFC line chart.
     * 
     * @param data 
     *         The data that is comma delimited
     * @param xLabel 
     *         The x-axis labels that is comma delimited
     * @param dataCount 
     *         the number of data points (and xLabels)
     * @param yMax 
     *         The maximum value of the y-axis
     * @param statPretty 
     *         The title of the chart
     * @param isShowMenu 
     *         Whether to show the flash chart menu or not
     * @return 
     *         The json data string for OFC chart
     */
    def generateJsonDataLineChart(data: String, xLabel: String,
            dataCount: Long, yMax: Long, statPretty: String, isShowMenu: Boolean, steps: Int, isFill: Boolean) : String =  {
        var xsteps = 0
        var ystepsStr = ""
        if (steps > 0) {
            xsteps = steps
        } else {
            xsteps = ((Math.floor(dataCount / X_STEP_INTERVAL)) + 1).asInstanceOf[Int]
        }
        if (yMax > 5) {
            var ysteps = ((Math.floor(yMax / Y_STEP_INTERVAL)) + 1).asInstanceOf[Int]
            ystepsStr = String.valueOf(ysteps)
        } else {
            ystepsStr = "0." + yMax
        }


        var jsonData = "{'elements': [{'type': '" + (if (isFill) "area" else "line") + "','colour': '#ff5c00'," + 
                "'dot-style':{'type': 'dot', 'dot-size': 2}," + 
                "'on-show':{'type':'pop-up','cascade':1,'delay':0.5}," + 
                "'values': [" + data + "],'tip': '#val#'"
        if (isFill) {
            jsonData += ",'fill':'#ff5c00','fill-alpha':0.5"
        }
        jsonData += "}]," 
        if (isShowMenu) {
            jsonData += "'menu':{'colour':'#e1e1e1','outline_colour':'#3a3a3a'," + 
                    "'values':[{'type':'text','text':'" + "bshare.charts.popup.saveas.csv" + 
                    "','javascript-function':'save_csv'}]},"
        }
        jsonData += "'title': {'text': '" + statPretty + "'},"
        jsonData += "'tooltip': {'shadow': true, 'stroke': 2, 'colour': '#6d6e71', 'background': '#e7e7e7', " + 
                "'title': '{font-size: 10px; font-weight: bold; color: #ff5c00}', " + 
                "'body': '{font-size: 10px; font-weight: bold; color: #000000}'},"
        jsonData += "'x_axis':{'labels': {'labels':[" + xLabel + "],'steps':" + xsteps + 
                "},'colour': '#6d6e71'},"
        jsonData += "'y_axis':{'steps':" + ystepsStr + ",'max':" + yMax + 
                ",'colour': '#6d6e71'},"
        // fix for y-axis labels numbers getting cut off
        jsonData += "'y_legend':{'text':'','style':'{font-size:5pxcolor:#ffffff}'}," 
        jsonData += "'bg_colour':'#FFFFFF'"
        jsonData += "}"

        return jsonData
    }
    
}