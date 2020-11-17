node.stripdebug()
require("ds18b20")

-----------------------------------------------
--- Set Variables ---
-----------------------------------------------
--- WIFI CONFIGURATION ---
local WIFI_SSID = "WIFI_SSID"
local WIFI_PASSWORD = "WIFI_PASSWORD"
local WIFI_SIGNAL_MODE = wifi.PHYMODE_N

--- IP CONFIG (Leave blank to use DHCP) ---
local ESP8266_IP = ""
local ESP8266_NETMASK = ""
local ESP8266_GATEWAY = ""
-----------------------------------------------

--- SERVER HOSTNAME ---
local SERVER_HOST = "SPRING_BACKEND"
-----------------------------------------------

local SENSOR_PIN = 1
local LED_PIN = 3
local led_status = gpio.HIGH

-- Initialising pin
gpio.mode(LED_PIN, gpio.OUTPUT)
gpio.write(LED_PIN, led_status)

wifi.eventmon.register(wifi.eventmon.STA_DISCONNECTED, function()
    print("\nSTA - STA_DISCONNECTED")
    node.restart()
    return
end)

wifi.eventmon.register(wifi.eventmon.STA_CONNECTED, function()
    print("\nSTA - STA_CONNECTED")
end)

wifi.eventmon.register(wifi.eventmon.STA_GOT_IP, 
function(T)
    print("Version 1.1")
    print("\nSTA - GOT IP"..
          "\nStation IP: "..T.IP..
          "\nSubnet mask: "..T.netmask..
          "\nGateway IP: "..T.gateway
          )
    if adc.force_init_mode(adc.INIT_VDD33)
    then
      node.restart()
      return -- don't bother continuing, the restart is scheduled
    end          

    ws = websocket.createClient()

    ws:on("connection", function()
      print('\nWS - GOT CONNECTION\n')

        tmr.alarm(0, 1000, 1, function() 
            ds18b20:read_temp(readout, SENSOR_PIN)
        end)
   
    end)

    ws:on("receive", function(_, msg, opcode)
      print('\nWS - got message:', msg, opcode) -- opcode is 1 for text message, 2 for binary
    end)
    
    ws:on("close", function(_, state)
      print('\nWS - connection closed', state)
      ws = nil -- required to Lua gc the websocket BBM
      wifi.sta.disconnect()
    end)

    
    ws:connect('ws://'..SERVER_HOST..'/report')
end)

--- Connect to the wifi network ---
wifi.setmode(wifi.STATION) 
wifi.setphymode(WIFI_SIGNAL_MODE)
wifi.sta.config({ssid=WIFI_SSID, pwd=WIFI_PASSWORD})
wifi.sta.connect()

if ESP8266_IP ~= "" then
 wifi.sta.setip({ip=ESP8266_IP,netmask=ESP8266_NETMASK,gateway=ESP8266_GATEWAY})
end
-----------------------------------------------

function readout(temp)
    
    local sensorReadings = {}
    --if ds18b20.sens then
        --print("Total number of DS18B20 sensors: ".. #ds18b20.sens)
        --for i, s in ipairs(ds18b20.sens) do
        --    print(string.format("  sensor #%d address: %s%s",  i, ('%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X'):format(s:byte(1,8)), s:byte(9) == 1 and " (parasite)" or ""))
        --end
    --end
     
    for addr, t in pairs(temp) do
        sensorReadings["temp"] = t
        sensorReadings["id"] = ('%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X'):format(addr:byte(1,8))
        sensorReadings["voltage"] = adc.readvdd33()
    end

    ws:send(sjson.encode(sensorReadings),1)
    print(sjson.encode(sensorReadings))
    blink()
end

function blink()
    if led_status == gpio.LOW then
        led_status = gpio.HIGH
    else
        led_status = gpio.LOW
    end

    gpio.write(LED_PIN, led_status)
end

collectgarbage ("collect")
