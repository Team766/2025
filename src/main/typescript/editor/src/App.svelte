<script>
  // Import dependencies
  import { JSONEditor } from 'svelte-jsoneditor';
  import NProgress from 'nprogress';
  import 'nprogress/nprogress.css';
  import { onMount } from 'svelte';
  import Split from 'split.js';

  // Declare reactive state variables using svelte runes
  let content = $state({ json: {} });
  let saveResult = $state(null);
  let isLoading = $state(true);
  let loadError = $state(null);
  let splitInstance = null;
  let saveToDisk = $state(false);

  // Run AFTER the component has been created and is in the DOM
  onMount(async () => {
    // Create an event listener to call saveConfig() on Ctrl + s or Cmd + s
    const keydownHandler = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 's') {
        e.preventDefault();
        saveConfig();
      }
    };
    document.addEventListener('keydown', keydownHandler, { capture: true });

    try {
      // Grab the current config from the API, making sure it's fresh and not cached
      const response = await fetch("/fancy-config-backend/load-json", {
        cache: "no-store",
        headers: {
          "Cache-Control": "no-cache"
        }
      });
      if (!response.ok) {
        throw new Error(`Failed to load config: ${response.statusText}`);
      }
      const data = await response.json();
      content = { json: data };
      isLoading = false;

      // Wait a bit of time to make sure everything is rendered before split.js is introduced
      await new Promise(resolve => setTimeout(resolve, 100));

      // Create a split.js instance, putting the editor and results tab on top of each other with a 70/30 split
      splitInstance = Split(['.editor-container', '.results'], {
        direction: 'vertical',
        sizes: [70, 30],
        minSize: [100, 50],
        gutterSize: 6,
        cursor: 'ns-resize'
      });

      // Run asynchronously after 200ms has passed
      setTimeout(() => {
        // Find the menu item in the DOM
        const menu = document.querySelector('.jse-menu');
        if (menu) {
          // If the menu is present, create a label to hold the checkbox
          const checkboxContainer = document.createElement('label');
          checkboxContainer.className = 'custom-menu-checkbox';

          // Create checkbox
          const checkbox = document.createElement('input');
          checkbox.type = 'checkbox';
          checkbox.id = 'save-disk';
          checkbox.checked = saveToDisk;
          checkbox.onchange = (e) => {
            saveToDisk = e.target.checked;
          };

          const label = document.createElement('span');
          label.textContent = 'Save to disk';

          // Add everything to the container
          checkboxContainer.appendChild(checkbox);
          checkboxContainer.appendChild(label);
          menu.appendChild(checkboxContainer);
        }
      }, 200);
    } catch (err) {
      loadError = err.message;
      isLoading = false;
    }

    // Return a cleanup function for svelte
    return () => {
      document.removeEventListener('keydown', keydownHandler, { capture: true });
      if (splitInstance) {
        splitInstance.destroy();
      }
    };
  });

  async function saveConfig() {
    // Start an animated loading screen
    NProgress.start();
    try {
      let jsonString;
      // Get the json from the editor as a string
      if (content.text !== undefined) {
        jsonString = content.text;
      } else {
        jsonString = JSON.stringify(content.json, null, 2);
      }

      // Parse the json string. If it throws a parseError, return and set the result to a failure
      try {
        JSON.parse(jsonString);
      } catch (parseError) {
        saveResult = {
          success: false,
          errors: [`Invalid JSON: ${parseError.message}`]
        };
        NProgress.done();
        return;
      }
      var api_url = "/fancy-config-backend/save-json"
      if (saveToDisk) {
        api_url = api_url + "?persistent=true"
      }
      const response = await fetch(api_url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: jsonString
      });
      
      const text = await response.text();
      if (response.ok) {
        saveResult = { success: true, message: text };
      } else {
        saveResult = { success: false, errors: text.split('\n').filter(Boolean) };
      }
    } catch (err) {
      saveResult = { success: false, errors: [err.message] };
    } finally {
      NProgress.done();
    }
  }

  function handleRenderMenu(items) {
    let compactIndex = null;
    items.forEach(function(item, index) {
      if (item.className === "jse-compact") {
        compactIndex = index;
      }
    });
    
    if (compactIndex !== null) {
      items.splice(compactIndex, 1);
    }
    
    return items.filter(item => {
      const isTableButton = (item.mode === 'table' || item.text === 'table');
      return !isTableButton;
    });
  }
</script>

<svelte:head>
  <title>Fancy Config Editor</title>
</svelte:head>

{#if isLoading}
  <div class="loading-screen">
    <div class="spinner"></div>
    <div class="loading-text">Loading configuration...</div>
  </div>
{:else if loadError}
  <div class="error-screen">
    <div class="error-icon">⚠️</div>
    <h2>Failed to Load Configuration</h2>
    <p class="error-message">{loadError}</p>
    <button onclick={() => window.location.reload()}>Retry</button>
  </div>
{:else}
  <div class="full-screen-wrapper">
    <div class="editor-container">
      <JSONEditor
        bind:content
        mode="text"
        onRenderMenu={handleRenderMenu}
      />
    </div>
    <div class="results" class:success={saveResult?.success}>
      {#if saveResult?.success}
        {saveResult.message}
      {:else if saveResult?.errors}
        {#each saveResult.errors as error}
          <div class="error-box">{error}</div>
        {/each}
      {:else}
        Press Ctrl+S to save
      {/if}
    </div>
  </div>
{/if}

<style>
  :global(html, body) {
    margin: 0;
    padding: 0;
    height: 100%;
    width: 100%;
    overflow: hidden;
    background: #ffffff;
  }

  .loading-screen {
    height: 100vh;
    width: 100vw;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;
  }

  .spinner {
    width: 50px;
    height: 50px;
    border: 5px solid #e0e0e0;
    border-top: 5px solid #0090f7;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }

  .loading-text {
    margin-top: 20px;
    font-size: 18px;
    color: #666;
  }

  .error-screen {
    height: 100vh;
    width: 100vw;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;
    padding: 20px;
    text-align: center;
  }

  .error-icon {
    font-size: 64px;
    margin-bottom: 20px;
  }

  .error-screen h2 {
    color: #333;
    margin: 0 0 15px 0;
  }

  .error-message {
    color: #666;
    margin-bottom: 30px;
    max-width: 600px;
  }

  .error-screen button {
    padding: 12px 24px;
    font-size: 16px;
    background: #0090f7;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }

  .error-screen button:hover {
    background: #0080e0;
  }

  .full-screen-wrapper {
    height: 100vh;
    width: 100vw;
    display: flex;
    flex-direction: column;
  }

  .editor-container,
  .results {
    overflow: auto;
  }

  .results {
    padding: 15px;
    background: #f5f5f5;
    color: #666;
  }

  .results.success {
    background: #1ba851;
    color: white;
    font-weight: bold;
  }

  .error-box {
    background: #ff5252;
    color: white;
    padding: 10px;
    margin-bottom: 10px;
    border-radius: 4px;
  }

  :global(.jse-main) {
    height: 100%;
    border: none !important;
  }

  :global(.gutter) {
    background: #ffffff;
  }

  :global(.gutter:hover) {
    background: #0090f7;
  }

  :global(.custom-menu-checkbox) {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 8px;
    cursor: pointer;
    user-select: none;
    margin-left: auto;
  }

  :global(.custom-menu-checkbox input[type="checkbox"]) {
    width: 16px;
    height: 16px;
    cursor: pointer;
    accent-color: #0090f7;
  }

  :global(.custom-menu-checkbox span) {
    font-size: 13px;
    font-weight: 500;
  }
</style>
