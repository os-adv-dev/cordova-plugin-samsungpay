#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

function addStringToXml() {
    const args = process.argv;
    let debugApiKey = null;
    let serviceId = null;
    let debugMode = null;

    for (const arg of args) {
        if (arg.includes('APP_SERVICE_ID')) {
            var stringArray = arg.split("=");
            serviceId = stringArray.slice(-1).pop();
        }

        if (arg.includes('APP_DEBUG_MODE')) {
            var stringArray = arg.split("=");
            debugMode = stringArray.slice(-1).pop();
        }

        if (arg.includes('APP_DEBUG_API_KEY')) {
            var stringArray = arg.split("=");
            debugApiKey = stringArray.slice(-1).pop();
        }
    }

    if (!serviceId) {
        console.error('❌ APP_SERVICE_ID not provided');
        return;
    }

    if (!debugApiKey) {
        console.error('❌ APP_DEBUG_API_KEY not provided');
        return;
    }

    if (!debugMode) {
        console.error('❌ APP_DEBUG_MODE not provided');
        return;
    }

    console.log("✅ Variable APP_SERVICE_ID: "+serviceId);
    console.log("✅ Variable APP_DEBUG_API_KEY: "+debugApiKey);
    console.log("✅ Variable APP_DEBUG_MODE: "+debugMode);

    // Path to strings.xml
    const stringXmlPath = path.join('platforms', 'android', 'app', 'src', 'main', 'res', 'values', 'strings.xml');

    console.log("✅ Path Strings XML : "+stringXmlPath);

    // Read and modify strings.xml
    fs.readFile(stringXmlPath, 'utf8', function(err, data) {
        if (err) {
            console.error('❌ Failed to read colors.xml:', err);
            return;
        }

        // Replace the values of existing keys
        data = data.replace(/<string name="app_service_id">[^<]*<\/string>/, `<string name="app_service_id">${serviceId}</string>`);
        data = data.replace(/<string name="app_debug_mode">[^<]*<\/string>/, `<string name="app_debug_mode">${debugMode}</string>`);
        data = data.replace(/<string name="app_spay_debug_api_key">[^<]*<\/string>/, `<string name="app_spay_debug_api_key">${debugApiKey}</string>`);

        // Write the updated content back to the file
        fs.writeFile(stringXmlPath, data, 'utf8', function(err) {
            if (err) {
                console.error('❌ Failed to write to strings.xml:', err);
                return;
            }
            console.log('✅ Updated values in strings.xml');
        });

        console.log("✅ Data updated: Strings XML : "+data);
    });
}

// If the script is run directly, execute the function
if (require.main === module) {
    addStringToXml();
}

// Export the function for external use
module.exports = addStringToXml;