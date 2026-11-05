// Karma configuration. `npm test` runs headed and watches; `npm run test:ci`
// runs once in ChromeHeadless with coverage thresholds enforced.
module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('karma-junit-reporter'),
      require('@angular-devkit/build-angular/plugins/karma'),
    ],
    client: {
      jasmine: {
        // Surface order-dependent tests early rather than in CI.
        random: true,
        failSpecWithNoExpectations: true,
      },
      clearContext: false,
    },
    jasmineHtmlReporter: {suppressAll: true},
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/ui'),
      subdir: '.',
      reporters: [{type: 'html'}, {type: 'text-summary'}, {type: 'lcovonly'}],
      // Ratchet: these are a floor, not a target. Raise them as suites land
      // so coverage can never regress. Target is 80/70/80/80.
      check: {
        global: {
          statements: 45,
          branches: 30,
          functions: 25,
          lines: 45,
        },
      },
    },
    junitReporter: {
      outputDir: require('path').join(__dirname, './coverage/ui'),
      outputFile: 'junit.xml',
      useBrowserName: false,
    },
    reporters: ['progress', 'kjhtml'],
    browsers: ['Chrome'],
    customLaunchers: {
      ChromeHeadlessCI: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage'],
      },
    },
    restartOnFileChange: true,
  });
};
