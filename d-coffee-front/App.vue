<script>
export default {
  onLaunch() {
    console.info('D-coffee app started')
  },
}
</script>

<style lang="scss">
@import './styles/variables.scss';

page {
  min-height: 100%;
  background: $color-cream;
  color: $color-ink;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
</style>
