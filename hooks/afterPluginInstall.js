#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const xml2js = require('xml2js');

// Define the path to strings.xml based on the platform structure
const platformPath = 'platforms/android/app/src/main';
const stringsXmlPath = path.join(__dirname, '..', platformPath, 'res/values/strings.xml');

// Path to strings.xml
const stringsXmlPathRoot = path.join('platforms', 'android', 'app', 'src', 'main', 'res', 'values', 'strings.xml');
console.log("Path: "+stringsXmlPathRoot);

const parser = new xml2js.Parser();
const builder = new xml2js.Builder();

const args = process.argv
var argAppServiceId;
var argDebugMode;
var argDebugApiKey;

    for (const arg of args) {  
      if (arg.includes('APP_SERVICE_ID')){
        var stringArray = arg.split("=");
        argAppServiceId = stringArray.slice(-1).pop();
        console.log("Value from APP_SERVICE_ID: "+argAppServiceId);
      }
      if (arg.includes('APP_DEBUG_MODE')){
        var stringArray = arg.split("=");
        argDebugMode = stringArray.slice(-1).pop();
        console.log("Value from APP_DEBUG_MODE: "+argDebugMode);
      }
      if (arg.includes('APP_DEBUG_API_KEY')){
        var stringArray = arg.split("=");
        argDebugApiKey = stringArray.slice(-1).pop();
        console.log("Value from APP_DEBUG_API_KEY: "+argDebugApiKey);
      }
    }

const variables = {
    APP_SERVICE_ID: argAppServiceId || 'default_value',
    APP_DEBUG_MODE: argDebugMode || 'default_value',
    APP_DEBUG_API_KEY: argDebugApiKey || 'default_value',
};

console.log("APP_SERVICE_ID "+variables.APP_SERVICE_ID);
console.log("APP_DEBUG_MODE "+variables.APP_DEBUG_MODE);
console.log("APP_DEBUG_API_KEY "+variables.APP_DEBUG_API_KEY);

fs.readFile(stringsXmlPath, 'utf-8', (err, data) => {
    if (err) {
        console.error('Error reading strings.xml:', err);
        process.exit(1);
    }

    parser.parseString(data, (err, result) => {
        if (err) {
            console.error('Error parsing strings.xml:', err);
            process.exit(1);
        }

        for (const variableName in variables) {
            if (variables.hasOwnProperty(variableName)) {
                const appServiceId = result.resources.string.find( (entry) => entry.$.name === 'app_service_id' );
                console.log("appServiceId >>> "+appServiceId);
                if (appServiceId) {
                    appServiceId._ = variables[variableName];
                }

                const appDebugMode = result.resources.string.find( (entry) => entry.$.name === 'app_debug_mode' );
                console.log("appDebugMode >>> "+appDebugMode);
                if (appDebugMode) {
                    appDebugMode._ = variables[variableName];
                }

                const appDebugApiKey = result.resources.string.find( (entry) => entry.$.name === 'app_spay_debug_api_key' );
                console.log("appDebugApiKey >>> "+appDebugApiKey);
                if (appServiceId) {
                    appServiceId._ = variables[variableName];
                }
            }
        }

        const xml = builder.buildObject(result);

        fs.writeFile(stringsXmlPath, xml, 'utf-8', (err) => {
            if (err) {
                console.error('Error writing strings.xml:', err);
                process.exit(1);
            }

            console.log('Variables added to strings.xml');
        });
    });
});
