module.exports = function(grunt) {
	
	var staticContentFolder = '../src/main/resources/SLING-INF/libs/sling/resource-editor-static-content';
	
	grunt.initConfig({
		env : {
		    build : {
		    	PHANTOMJS_BIN : 'node_modules/karma-phantomjs-launcher/node_modules/phantomjs/lib/phantom/bin/phantomjs',
		    }
		},
	    less: {
	      compileCore: {
	        options: {
	          strictMath: true,
	          sourceMap: true,
	          outputSourceFiles: true,
	          sourceMapURL: 'bootstrap.css.map',
	          sourceMapFilename: staticContentFolder+'/generated/css/bootstrap.css.map'
	        },
	        src: '../src/main/less/reseditor.less',
	        dest: staticContentFolder+'/generated/css/bootstrap.css'
	      }
	    }, 
	    watch: {
			less : {
				files : '../src/main/less/**/*.less',
				tasks : [ 'less' ],
			}
	    },
	    _comment:'The google web fonts could be downloaded and copied via grunt-goog-webfont-dl. But goog-webfont-dl directly points to the global #!/usr/bin/env node and not to the local one.',
	    copy: {
	    	js_dependencies: {
		        files: [
		          {
		            expand: true,     // Enable dynamic expansion.
		            cwd: 'node_modules/',      // Src matches are relative to this path.
		            src: [
		                  'bootstrap/dist/js/bootstrap.min.js',
		                  'select2/select2.min.js',
		                  'jquery/dist/jquery.min.js',
		                  'bootbox/bootbox.min.js',
		                  'jstree/dist/jstree.min.js'
		                 ], // Actual pattern(s) to match.
		            dest: staticContentFolder+'/generated/3rd_party/js',   // Destination path prefix.
		            flatten: true
		          },
		        ],
		      },
    	css_dependencies: {
	        files: [
	          {
	            expand: true,     // Enable dynamic expansion.
	            cwd: 'node_modules/',      // Src matches are relative to this path.
	            src: [
	                  'select2/select2.css',
	                  'select2/select2.png',
	                  'animate.css/animate.min.css',
	                 ], // Actual pattern(s) to match.
	            dest: staticContentFolder+'/generated/3rd_party/css',   // Destination path prefix.
	            flatten: true
	          },
	        ],
	      }
	    },
	    karma: {
	    	options: {
	    	    runnerPort: 9999,
	    	    singleRun: true,
	    	    browsers: ['Chrome', 'Firefox', 'PhantomJS'],
	    	    plugins : ['karma-jasmine', 'karma-phantomjs-launcher', 'karma-chrome-launcher', 'karma-firefox-launcher', 'karma-ie-launcher'],
	    	    frameworks: ['jasmine'],
			    files: ['../src/test/javascript/spec/*spec.js',
			            staticContentFolder+'/js/3rd_party/jquery.min.js',
			            staticContentFolder+'/js/**/*.js'
			           ]
	    	},  
	    	desktop_build: {
	    	    singleRun: true,
	    	    browsers: ['Chrome', 'Firefox']
	    	},
	    	build: {
	    	    singleRun: true,
	    	    browsers: ['PhantomJS']
	    	},
	    	watch: {
	    	    reporters: 'dots',
	    	    autoWatch: true,
	    	    background: true,
	    	    singleRun: false
	    	}
	    },
        webdriver: {
            options: {
            },
            chrome: {
                tests: ['../src/test/javascript/e2e/spec/**/*spec.js'],
                options: {
                    // overwrite default settings 
                    desiredCapabilities: {
                        browserName: 'chrome'
                    }
                }
            },
            firefox: {
                tests: ['../src/test/javascript/e2e/spec/**/*spec.js'],
                options: {
                    // overwrite default settings 
                    desiredCapabilities: {
                        browserName: 'firefox'
                    }
                }
            }
          }
	})
	
    // These plugins provide necessary tasks.
    require('load-grunt-tasks')(grunt, { scope: 'devDependencies' });

	grunt.registerTask('setup', ['env:build']);
	grunt.registerTask('build', ['setup', 'less', 'copy', 'karma:build']);

	grunt.registerTask('default', ['build']);
	

    grunt.registerTask('desktop_build', ['setup', 'less', 'copy', 'karma:desktop_build', 'webdriver:chrome', 'webdriver:firefox']);
};