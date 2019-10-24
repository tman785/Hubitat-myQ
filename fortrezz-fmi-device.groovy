/**
 *  FortrezZ Flow Meter Interface
 *
 *  Copyright 2016 FortrezZ, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Contributors: FortrezZ (original - Daniel Kurin), jscgs350
 *
 *  Updates:
 *  -------
 *  07-06-2016 : Original commit.
 *  07-13-2016 : Modified the device handler for my liking, primarly for looks and feel.
 *  07-16-2016 : Changed GPM tile to be more descriptive during water flow, and then to show cumulative and last used gallons.
 *  07-23-2016 : Added tracking for highest recorded usage in gallons, and added actions for tiles to reset high values.  Added Reset Meter tile.
 *  08-07-2016 : Fixed GPM calculation error whenever the reporting threshold was less than 60 seconds.
 *  08-08-2016 : Moved where "waterState" gets defined (none, flow, highflow) - from AlarmReport to the MeterReport section since this DH handles this alarm condition differently than FortrezZ's original design.
 *  08-11-2016 : Fixed decimal positions so that only 2 positions are displayed vs as many as 9, 10, or more.  Minor cosmetic changes as well.
 *  08-20-2016 : Changed how parameters are handled (via Updated section now) and removed unneeded code due to that change.
 *  08-21-2016 : Removed the Updated section because ST would execute Configure twice for some reason.  User needs to tap on the Config tile after parameters are changed.
 *  08-27-2016 : Modified the device handler for my liking, primarly for looks and feel for some of the tiles.
 *  08-28-2016 : Reverted back to original gpm flow calculation instead of using weighted average due to large flow rate calculations (under review)
 *  08-29-2016 : Updated the resetMeter() section to get it working, and to update a status tile with the date a reset was last performed.
 *  08-31-2016 : Cleaned up unused code.  Used carouselTile for showing charts.  This is helpful after resetting the meter and user wanted to see previous charts.
 *  09-01-2016 : Added a few new attributes (ending in LastReset) to capture high values prior to being reset in case user needs to know or forgot to save.
 *  09-01-2016 : Created another user preference for a custom device ID that causes a new set of charts to be created as soon as the preference is defined.
 *  09-01-2016 : Moved where data is sent to the cloud to address data issues when reporting threshold is not 60 seconds.
 *  09-02-2016 : Moved and resized tiles around for a cleaner look (moved stats row up and resized to 2wx1h)
 *  09-12-2016 : Every so often a crazy high delta would be sent, so added a check for a not so realistic value.
 *  10-03-2016 : When the meter is reset, and if a custom ID is defined by the user, new charts will be created.
 *  10-05-2016 : Changed the chart selection process from toggling through each via a single tile, to a tile for each chart mode/type. Taping on the same tile refreshes the chart.
 *  11-11-2016 : Cleaned up code where meter values are assessed. (physicalgraph.zwave.commands.meterv3.MeterReport)
 *  01-08-2017 : Added code for Health Check capabilities/functions, and cleaned up code.
 *  03-11-2017 : Changed from valueTile to standardTile for three tiles since ST's mobile app v2.3.x broke the ability for valueTiles to initiate an action.
 *  03-29-2017 : Made changes to account for ST v2.3.1 bugs with text rendering.
 *  05-10-2017 : Updated code to use different attribute names from what FortrezZ is using, and to revert to their original purpose so that their SmartApps will work with this DTH.
 *  06-10-2017 : Changed to "http" from "https" for the URL for post/get functions because of certificate issues FortrezZ's site is having.  Will change back once fixed.
 *  06-12-2017 : Updated the updated() section to automatically run Configure after tapping on Done in the Preferences page.
 *  09-27-2017 : Changed Ping (health Check) to refresh temperature instead of updating charts.
 *  09-23-2017 : Changed layout to look like my Zooz DTH, cleaned up code a lot.
 *  10-03-2017 : More cosmetic changes to the main tile.
 *  10-04-2017 : Color changes on main tile, and fixed reset log messages and how they're processed.  (more of a workaround for now)
 *  10-07-2017 : Changed history tile from standard to value to resolve iOS rendering issue.
 *  11-10-2017 : Changed a few tiles from standard to value because they look better on iOS and still look fine on Android.
 *  02-23-2018 : Commented out line 440 (was 439) so that any website performance issues with ST or FortrezZ don't generate a SocketTimeoutException error.
 *  03-15-2018 : Reverted change made on 2-23-2018.
 *  09-20-2018 : Changes for new app (ongoing).
 *  09-23-2018 : Changed main tile layout, added a couple new tiles to show what use to be in secondary_control values.
 *  10-09-2018 : Cleaned up code.
 *  01-12-2019 : Cleaned up a lot of code.
 *  02-05-2019 : Added/updated error checking for negative delta values, crazy high delta values, duplicate current and previous flow values, and when current flow value is less than the previous flow value.
 *  03-01-2019 : Fixed history reporting error with values when meter, high gallons used, or highest GPM was reset.
 *
 */
metadata {
	definition (name: "My FortrezZ Flow Meter Interface", namespace: "jsconstantelos", author: "Daniel Kurin", ocfDeviceType: "x.com.st.d.energymeter") {
		capability "Battery"
		capability "Energy Meter"
        capability "Power Meter"
		capability "Image Capture"
		capability "Temperature Measurement"
        capability "Sensor"
        capability "Configuration"
        capability "Actuator"        
        capability "Polling"
        capability "Refresh"
        capability "Health Check"
        
        attribute "gpm", "number"
		attribute "gpmHigh", "number"
        attribute "gpmHighValue", "number"
		attribute "gpmTotal", "number"
        attribute "gpmLastUsed", "number"
		attribute "gpmHighLastReset", "number"
        attribute "cumulativeLastReset", "number"
        attribute "gallonHigh", "number"
        attribute "gallonHighValue", "number"
        attribute "gallonHighLastReset", "number"
        attribute "alarmState", "string"
        attribute "chartMode", "string"
        attribute "lastThreshhold", "number"
        attribute "lastReset", "string"

        command "chartMode"
        command "take1"
        command "take7"
        command "take28"
        command "resetgpmHigh"
        command "resetgallonHigh"
        command "resetMeter"
        command "fixChart"
        command "reset"

	    fingerprint deviceId: "0x2101", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x73, 0x71, 0x85, 0x59, 0x32, 0x31, 0x70, 0x80, 0x7A"
	}
    
    preferences {
       input "debugOutput", "boolean", title: "Enable debug logging?", defaultValue: false, displayDuringSetup: true
       input "reportThreshhold", "decimal", title: "Reporting Rate Threshhold", description: "The time interval between meter reports while water is flowing. 6 = 60 seconds, 1 = 10 seconds. Options are 1, 2, 3, 4, 5, or 6.", defaultValue: 1, required: false, displayDuringSetup: true
       input "gallonThreshhold", "decimal", title: "High Flow Rate Threshhold", description: "Flow rate (in gpm) that will trigger a notification.", defaultValue: 5, required: false, displayDuringSetup: true
       input("registerEmail", type: "email", required: false, title: "Email Address", description: "Register your device with FortrezZ", displayDuringSetup: true)
       input("customID", required: false, title: "Custom ID (CAUTION: ADVANCED USERS ONLY. Causes ALL charts to reset.  Leave empty for default.)", description: "Default is empty. This will reset ALL charts!", defaultValue: "", displayDuringSetup: false)
    }

	tiles(scale: 2) {
		multiAttributeTile(name: "gpm", type: "generic", width: 6, height: 4, canChangeIcon: true, decoration: "flat"){
			tileAttribute("device.gpm", key: "PRIMARY_CONTROL") {
				attributeState "gpm", label: '${currentValue} GPM',
						backgroundColors: [
								[value: 0, color: "#999999"],
								[value: 0.1, color: "#51afdb"]
						]
			}
            tileAttribute ("device.alarmState", key: "SECONDARY_CONTROL") {
                attributeState("alarmState", label:'${currentValue}', icon: "st.alarm.alarm.alarm")
            }
		}               
        carouselTile("chartCycle", "device.image", width: 6, height: 3) { }
		standardTile("dayChart", "device.chartMode", width: 2, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "day", label:'24 Hour View', action: 'take1', icon: "https://raw.githubusercontent.com/jsconstantelos/SmartThings/master/img/24-hour-clockv2.png"
		}
		standardTile("weekChart", "device.chartMode", width: 2, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "week", label:'Daily View', action: 'take7', icon: "https://raw.githubusercontent.com/jsconstantelos/SmartThings/master/img/7day.png"
		}
		standardTile("monthChart", "device.chartMode", width: 2, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "month", label:'4 Week View', action: 'take28', icon: "https://raw.githubusercontent.com/jsconstantelos/SmartThings/master/img/monthv2.png"
		}
		valueTile("temperature", "device.temperature", width: 2, height: 1) {
            state("temperature", label:'${currentValue}°', action:"refresh.refresh",
                backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }             
		valueTile("gpmTotal", "device.gpmTotal", inactiveLabel: false, width: 3, height: 1, decoration: "flat") {
			state "default", label:'Total Usage:\n${currentValue} gallons'
		}
		valueTile("gpmLastUsed", "device.gpmLastUsed", inactiveLabel: false, width: 3, height: 1, decoration: "flat") {
			state "default", label:'Last Used:\n${currentValue} gallons'
		}
		valueTile("gpmHigh", "device.gpmHigh", inactiveLabel: false, width: 3, height: 1, decoration: "flat") {
			state "default", label:'Highest flow:\n${currentValue}', action: 'resetgpmHigh'
		}
        valueTile("gallonHigh", "device.gallonHigh", inactiveLabel: false, width: 3, height: 1, decoration: "flat") {
			state "default", label:'Highest usage:\n${currentValue}', action: 'resetgallonHigh'
		}   
        valueTile("lastReset", "lastReset", inactiveLabel: false, decoration: "flat", width: 5, height: 1) {
			state "lastReset", label:'${currentValue}'
		}
		valueTile("powerState", "device.powerState", width: 2, height: 1) { 
			state "reconnected", label: "On", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "disconnected", label: "Off", icon: "st.switches.switch.off", backgroundColor: "#ffa81e"
			state "batteryReplaced", icon:"http://swiftlet.technology/wp-content/uploads/2016/04/Full-Battery-96.png", backgroundColor:"#cccccc"
			state "noBattery", icon:"http://swiftlet.technology/wp-content/uploads/2016/04/No-Battery-96.png", backgroundColor:"#cc0000"
		}
		standardTile("battery", "device.battery", inactiveLabel: false, width: 2, height:1) {
			state "battery", label:'${currentValue}%', unit:"", icon: "https://raw.githubusercontent.com/jsconstantelos/SmartThings/master/img/battery-icon-614x460.png"
		}
        standardTile("zeroTile", "device.zero", width: 3, height:1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "zero", label:'Reset Meter', action: 'resetMeter', icon: "st.secondary.refresh-icon"
		}
		standardTile("configure", "device.configure", width: 3, height:1, inactiveLabel: false, decoration: "flat") {
			state "configure", label: "Configure\nDevice", action: "configuration.configure", icon: "st.secondary.tools"
		}
		valueTile("history", "device.history", decoration:"flat", width: 6,height:2) {
			state "history", label:'${currentValue}'
		}
		standardTile("errorIcon", "device.errorHist", decoration:"flat", width: 1,height:1) {
			state "default", icon: "st.alarm.alarm.alarm"
		}
		valueTile("errorHist", "device.errorHist", decoration:"flat", width: 4,height:1) {
			state "errorHist", label:'${currentValue}'
		}
		standardTile("waterState", "device.waterState", width: 1, height: 1) { 
			state "none", icon:"http://cdn.device-icons.smartthings.com/valves/water/closed@2x.png", backgroundColor:"#999999", label: "No Flow"
			state "flow", icon:"http://cdn.device-icons.smartthings.com/valves/water/open@2x.png", backgroundColor:"#51afdb", label: "Flow"
			state "highflow", icon:"http://cdn.device-icons.smartthings.com/alarm/water/wet@2x.png", backgroundColor:"#ff0000", label: "High Flow"
		}
		main (["waterState"])
		details(["gpm", "gpmTotal", "gpmLastUsed", "gallonHigh", "gpmHigh", "powerState", "temperature", "battery", "dayChart", "weekChart", "monthChart", "chartCycle", "errorIcon", "errorHist", "errorIcon", "history", "configure", "zeroTile"])
	}
}

def installed() {
    state.meterResetDate = ""
    state.debug = ("true" == debugOutput)
}

def updated(){
	state.debug = ("true" == debugOutput)
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 30 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    response(configure())
}

// parse events into attributes
def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
	    results << createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [ 0x80: 1, 0x84: 1, 0x71: 2, 0x72: 1 ])
		if (cmd) {
			results << createEvent( zwaveEvent(cmd) )
		}
	}
//    log.debug "Data parsed to : ${results.inspect()}"
	return results
}

def take1() {
    api("24hrs", "") {
        if (state.debug) log.debug("Image captured")
        if(it.headers.'Content-Type'.contains("image/png")) {
            if(it.data) {
                storeImage(getPictureName("24hrs"), it.data)
            }
        }
    }
}

def take7() {
    api("7days", "") {
        if (state.debug) log.debug("Image captured")
        if(it.headers.'Content-Type'.contains("image/png")) {
            if(it.data) {
                storeImage(getPictureName("7days"), it.data)
            }
        }
    }
}

def take28() {
    api("4weeks", "") {
        if (state.debug) log.debug("Image captured")
        if(it.headers.'Content-Type'.contains("image/png")) {
            if(it.data) {
                storeImage(getPictureName("4weeks"), it.data)
            }
        }
    }
}

def poll() {
    refresh()
}

def reset() {
    resetMeter()
}

def resetMeter() {
	log.debug "Resetting water meter..."
    def dispValue
    def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    if (customID != null) {
    	state.meterResetDate = new Date().format("MMddyyhmmssa", location.timeZone)
    } else {
    	state.meterResetDate = ""
    }
    sendEvent(name: "cumulativeLastReset", value: device.currentState('gpmTotal')?.doubleValue+" gal", displayed: false)
    sendEvent(name: "gpmLastUsed", value: 0, displayed: false)
    sendEvent(name: "gpmTotal", value: 0, displayed: false)
    sendEvent(name: "energy", value: 0, unit: "kWh", displayed: false)
    def cmds = delayBetween([
	    zwave.meterV3.meterReset().format()
    ])
    resetgpmHigh()
    resetgallonHigh()
    dispValue = "Meter was reset on "+timeString
    sendEvent(name: "lastReset", value: dispValue as String, displayed: false)
	def historyDisp = ""
    historyDisp = "${device.currentState('lastReset')?.value}\nCummulative at last reset: ${device.currentState('cumulativeLastReset')?.value} gal\nHighest gallons used at last reset: ${device.currentState('gallonHighLastReset')?.value}\nHighest GPM at last reset: ${device.currentState('gpmHighLastReset')?.value}"
    sendEvent(name: "history", value: historyDisp, displayed: false)
    sendEvent(name: "alarmState", value: "The meter was just reset", descriptionText: text, displayed: true)
    sendEvent(name: "errorHist", value: "The meter was just reset at "+timeString, descriptionText: text, displayed: true)
    take1()
    return cmds
}

def resetgpmHigh() {
	log.debug "Resetting high value for GPM..."
    def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    sendEvent(name: "gpmHighLastReset", value: device.currentState('gpmHighValue')?.doubleValue+" gpm", displayed: false)
    sendEvent(name: "gpmHigh", value: "(resently reset)")
    sendEvent(name: "gpmHighValue", value: 0)
	def historyDisp = ""
    historyDisp = "${device.currentState('lastReset')?.value}\nCummulative at last reset: ${device.currentState('cumulativeLastReset')?.value} gal\nHighest gallons used at last reset: ${device.currentState('gallonHighLastReset')?.value}\nHighest GPM at last reset: ${device.currentState('gpmHighLastReset')?.value}"
    sendEvent(name: "history", value: historyDisp, displayed: false)
    sendEvent(name: "alarmState", value: "GPM high value was reset", descriptionText: text, displayed: true)
}

def resetgallonHigh() {
	log.debug "Resetting high value for gallons used..."
    def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    sendEvent(name: "gallonHighLastReset", value: device.currentState('gallonHighValue')?.doubleValue+" gals", displayed: false)
    sendEvent(name: "gallonHigh", value: "(resently reset)")
    sendEvent(name: "gallonHighValue", value: 0)
	def historyDisp = ""
    historyDisp = "${device.currentState('lastReset')?.value}\nCummulative at last reset: ${device.currentState('cumulativeLastReset')?.value} gal\nHighest gallons used at last reset: ${device.currentState('gallonHighLastReset')?.value}\nHighest GPM at last reset: ${device.currentState('gpmHighLastReset')?.value}"
    sendEvent(name: "history", value: historyDisp, displayed: false)
    sendEvent(name: "alarmState", value: "Gals used high value was reset", descriptionText: text, displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	if (state.debug) log.debug "Getting temperature data..."
	def map = [:]
	if(cmd.sensorType == 1) {
		map = [name: "temperature"]
        if(cmd.scale == 0) {
        	map.value = getTemperature(cmd.scaledSensorValue)
        } else {
	        map.value = cmd.scaledSensorValue
        }
        map.unit = location.temperatureScale
        sendEvent(name: "alarmState", value: "Temperature Updated", descriptionText: text, displayed: false)
	}
	return map
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	if (state.debug) log.debug "scaledMeterValue is ${cmd.scaledMeterValue}"
    if (state.debug) log.debug "scaledPreviousMeterValue is ${cmd.scaledPreviousMeterValue}"
    def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    def delta = Math.round((((cmd.scaledMeterValue - cmd.scaledPreviousMeterValue) / (reportThreshhold*10)) * 60)*100)/100 //rounds to 2 decimal positions
    if (delta < 0) { //There should never be any negative values
			if (state.debug) log.debug "We just detected a negative delta value that won't be processed: ${delta}"
            sendEvent(name: "errorHist", value: "Negative value detected on "+timeString, descriptionText: text, displayed: true)
            return
    } else if (delta > 60) { //There should never be any crazy high gallons as a delta, even at 1 minute reporting intervals.  It's not possible unless you're a firetruck.
    		if (state.debug) log.debug "We just detected a crazy high delta value that won't be processed: ${delta}"
            sendEvent(name: "errorHist", value: "Crazy high delta value detected on "+timeString, descriptionText: text, displayed: true)
            return
    } else if (delta == 0) {
    		if (state.debug) log.debug "Flow has stopped, so process what the meter collected."
            if (cmd.scaledMeterValue == device.currentState('gpmTotal')?.doubleValue) {
            	if (state.debug) log.debug "Current and previous flow values were the same, so skip processing."
                sendEvent(name: "errorHist", value: "Current and previous flow values were the same at "+timeString, descriptionText: text, displayed: true)
                return
            }
            if (cmd.scaledMeterValue < device.currentState('gpmTotal')?.doubleValue) {
            	if (state.debug) log.debug "Current flow value is less than the previous flow value and that should never happen, so skip processing."
                sendEvent(name: "errorHist", value: "Current flow value was less than the previous flow value at "+timeString, descriptionText: text, displayed: true)
                return
            }
			def prevCumulative = cmd.scaledMeterValue - device.currentState('gpmTotal')?.doubleValue
            sendDataToCloud(prevCumulative)
			if (prevCumulative > device.currentState('gallonHighValue')?.doubleValue) {
                sendEvent(name: "gallonHigh", value: String.format("%3.1f",prevCumulative)+" gallons on"+"\n"+timeString as String, displayed: true)
                sendEvent(name: "gallonHighValue", value: String.format("%3.1f",prevCumulative), displayed: false)
            }
            sendEvent(name: "power", value: delta, displayed: false)  // This is only used for SmartApps that need Power capabilities.
            sendEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh", displayed: false) // This is only used for SmartApps that need Energy capabilities.
            sendEvent(name: "gpmTotal", value: cmd.scaledMeterValue, displayed: false)
            sendEvent(name: "gpmLastUsed", value: String.format("%3.1f",prevCumulative), displayed: true)
            sendEvent(name: "waterState", value: "none", displayed: true)
            sendEvent(name: "gpm", value: delta, displayed: false)
            sendEvent(name: "alarmState", value: "Normal Operation", descriptionText: text, displayed: true)
            return
    	} else {
        	sendEvent(name: "gpm", value: delta, displayed: false)
            sendEvent(name: "power", value: delta, displayed: false)  // This is only used for SmartApps that need Power capabilities.
            if (state.debug) log.debug "flowing at ${delta}"
            if (delta > device.currentState('gpmHighValue')?.doubleValue) {
                sendEvent(name: "gpmHigh", value: String.format("%3.1f",delta)+" gpm on"+"\n"+timeString as String, displayed: true)
                sendEvent(name: "gpmHighValue", value: String.format("%3.1f",delta), displayed: false)
            }
        	if (delta > gallonThreshhold) {
            	sendEvent(name: "waterState", value: "highflow")
                sendEvent(name: "alarmState", value: "High Flow Detected!", descriptionText: text, displayed: true)
        	} else {
        		sendEvent(name: "waterState", value: "flow")
                sendEvent(name: "alarmState", value: "Water is flowing", descriptionText: text, displayed: true)
			}
            return
    }
	return
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	def map = [:]
    if (cmd.zwaveAlarmType == 8) { // Power Alarm
        if (cmd.zwaveAlarmEvent == 2) { // AC Mains Disconnected
            sendEvent(name: "powerState", value: "disconnected")
            sendEvent(name: "alarmState", value: "Mains Disconnected!", descriptionText: text, displayed: true)
        } else if (cmd.zwaveAlarmEvent == 3) { // AC Mains Reconnected
			sendEvent(name: "powerState", value: "reconnected")
            sendEvent(name: "alarmState", value: "Mains Reconnected", descriptionText: text, displayed: true)
        } else if (cmd.zwaveAlarmEvent == 0x0B) { // Replace Battery Now
			sendEvent(name: "powerState", value: "noBattery")
            sendEvent(name: "alarmState", value: "Replace Battery Now", descriptionText: text, displayed: true)
        } else if (cmd.zwaveAlarmEvent == 0x00) { // Battery Replaced
			sendEvent(name: "powerState", value: "batteryReplaced")
            sendEvent(name: "alarmState", value: "Battery Replaced", descriptionText: text, displayed: true)
        }
    }
    else if (cmd.zwaveAlarmType == 4) {
    	map.name = "heatState"
        if (cmd.zwaveAlarmEvent == 0) {
            sendEvent(name: "alarmState", value: "Normal Operation", descriptionText: text, displayed: true)
        } else if (cmd.zwaveAlarmEvent == 1) {
            map.value = "overheated"
            sendEvent(name: "alarmState", value: "Overheating Detected!", descriptionText: text, displayed: true)
        } else if (cmd.zwaveAlarmEvent == 5) {
            map.value = "freezing"
            sendEvent(name: "alarmState", value: "Freezing Detected!", descriptionText: text, displayed: true)
        }
    }
	return map
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	if(cmd.batteryLevel == 0xFF) {
		map.name = "battery"
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.displayed = true
	} else {
		map.name = "battery"
		map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
		map.unit = "%"
		map.displayed = false
	}
	return map
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "COMMAND CLASS: $cmd"
}

def sendDataToCloud(double data) {
	def formatData = String.format("%3.1f",data)
	if (state.debug) log.debug "Sending data ${formatData} to the cloud..."
	def meterID
    if (customID != null) {
    	meterID = customID+state.meterResetDate
    } else {
    	meterID = device.id+state.meterResetDate
    }
    if (state.debug) log.debug meterID
    def params = [
        uri: "http://iot.swiftlet.technology",
        path: "/fortrezz/post.php",
        body: [
            id: meterID,
            value: formatData,
            email: registerEmail
        ]
    ]
    try {
        httpPostJson(params) { resp ->
            resp.headers.each {
                //log.debug "${it.name} : ${it.value}"
            }
            //log.debug "query response: ${resp.data}"
        }
    } catch (e) {
        log.debug "something went wrong: $e"
    }
    take1()
}

def getTemperature(value) {
	if(location.temperatureScale == "C"){
		return value
    } else {
        return Math.round(celsiusToFahrenheit(value))
    }
}

private getPictureName(category) {
  def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
  def name = "image" + "_$pictureUuid" + "_" + category + ".png"
  return name
}

def api(method, args = [], success = {}) {
	def meterID
    if (customID != null) {
    	meterID = customID+state.meterResetDate
    } else {
    	meterID = device.id+state.meterResetDate
    }
    def methods = [
      "24hrs":      [uri: "http://iot.swiftlet.technology/fortrezz/chart.php?uuid=${meterID}&tz=${location.timeZone.ID}&type=1", type: "get"],
      "7days":      [uri: "http://iot.swiftlet.technology/fortrezz/chart.php?uuid=${meterID}&tz=${location.timeZone.ID}&type=2", type: "get"],
      "4weeks":     [uri: "http://iot.swiftlet.technology/fortrezz/chart.php?uuid=${meterID}&tz=${location.timeZone.ID}&type=3", type: "get"],
    ]
    def request = methods.getAt(method)
    return doRequest(request.uri, request.type, success)
}

private doRequest(uri, type, success) {
  if (state.debug) log.debug(uri)
  if(type == "post") {
    httpPost(uri , "", success)
  } else if(type == "get") {
    httpGet(uri, success)
  }
}

// PING is used by Device-Watch in attempt to reach the Device
def ping() {
    refresh()
}

def refresh() {
    if (state.debug) log.debug "${device.label} refresh"
	delayBetween([
        zwave.sensorMultilevelV5.sensorMultilevelGet().format(),
        zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
	])
    def statusTextmsg = ""
    def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    statusTextmsg = "Last refreshed at "+timeString
    sendEvent(name:"statusText", value:statusTextmsg, displayed: false)
}

def configure() {
	log.debug "Configuring FortrezZ flow meter interface (FMI)..."
	log.debug "Setting reporting interval to ${reportThreshhold}"
	log.debug "Setting gallon threshhold to ${gallonThreshhold}"
    sendEvent(name: "lastThreshhold", value: gallonThreshhold, displayed: false)
    def cmds = delayBetween([
		zwave.configurationV2.configurationSet(configurationValue: [(int)Math.round(reportThreshhold)], parameterNumber: 4, size: 1).format(),
    	zwave.configurationV2.configurationSet(configurationValue: [(int)Math.round(gallonThreshhold*10)], parameterNumber: 5, size: 1).format()
    ],200)
    log.debug "Configuration report for FortrezZ flow meter interface (FMI): '${cmds}'"
    cmds
}
